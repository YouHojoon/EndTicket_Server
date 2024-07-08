package ac.kr.smu.endTicket.auth.service

import ac.kr.smu.endTicket.auth.config.property.JWTProperties
import ac.kr.smu.endTicket.auth.constant.RedisConstant
import ac.kr.smu.endTicket.auth.domain.exception.RefreshTokenExpiredException
import ac.kr.smu.endTicket.auth.domain.exception.UserExpiredException
import ac.kr.smu.endTicket.auth.infra.grpc.validateAccessTokenResponseOf
import ac.kr.smu.endTicket.auth.infra.jwt.createAccessToken
import ac.kr.smu.endTicket.auth.infra.jwt.createRefreshToken
import ac.kr.smu.endTicket.auth.infra.jwt.parseJwtSignedClaims
import ac.kr.smu.endTicket.auth.ui.response.TokenResponse
import ac.kr.smu.endticket.protobuf.AccessToken
import ac.kr.smu.endticket.protobuf.TokenServiceGrpc
import ac.kr.smu.endticket.protobuf.ValidateAccessTokenResponse
import io.grpc.StatusRuntimeException
import io.grpc.stub.StreamObserver
import io.jsonwebtoken.*
import io.jsonwebtoken.security.Keys
import io.jsonwebtoken.security.SignatureException
import net.devh.boot.grpc.server.service.GrpcService
import org.slf4j.LoggerFactory
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Service
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * 토큰 기능을 처리하는 클래스
 * @property redisTemplate redis를 사용하기 위한 객체
 * @property jwtProperties JWT 토큰 관련 설정
 */
@Service
@GrpcService
class TokenService(
    private val redisTemplate: RedisTemplate<String, String>,
    private val jwtProperties: JWTProperties,
) : TokenServiceGrpc.TokenServiceImplBase() {
    private val log = LoggerFactory.getLogger(TokenService::class.java)

    /**
     * JWT를 서명하기 위한 key
     */
    private val key = Keys.hmacShaKeyFor(jwtProperties.secret.toByteArray())

    /**
     * access 토큰을 검증하는 메소드
     * @param request 검증할 AccessToken
     * @param responseObserver 결과를 전달받을 옵저버
     */
    override fun validateAccessToken(
        request: AccessToken,
        responseObserver: StreamObserver<ValidateAccessTokenResponse>,
    ) {
        val (userId, status, message) =
            try {
                val userId = parseUserId(request.token.split(" ").last())

                checkUserNotExpired(userId)

                Triple(userId, 200, null)
            } catch (e: ExpiredJwtException) {
                Triple(null, 401, "토큰이 만료됐습니다.")
            } catch (e: SignatureException) {
                Triple(null, 400, "토큰 서명 검증에 실패했습니다.")
            } catch (e: UnsupportedJwtException) {
                Triple(null, 400, "올바르지 않은 토큰입니다.")
            } catch (e: StatusRuntimeException) {
                Triple(null, 500, e.message)
            } catch (e: UserExpiredException) {
                Triple(null, 401, e.message)
            }
        
        responseObserver.onNext(validateAccessTokenResponseOf(userId, status, message))
        responseObserver.onCompleted()
    }

    /**
     * 사용자의 토큰을 만료시키는 메소드
     * @param userId 사용자 id
     */
    fun expireAccessAndRefreshToken(userId: Long) = redisTemplate.opsForSet().add(RedisConstant.EXPIRED_USERS_REDIS_KEY, userId.toString())

    /**
     * 토큰 생성 기능
     * @param userId 사용자 번호
     * @return JWT 토큰 발급
     * @throws UserExpiredException 만료된 사용자일 시
     */
    fun createAccessAndRefreshToken(userId: Long): TokenResponse {
        checkUserNotExpired(userId)

        val issuedAt = Date()
        val accessToken = Jwts.builder().createAccessToken(key, userId, issuedAt, jwtProperties.accessTokenExpiration)
        val refreshToken = Jwts.builder().createRefreshToken(key, issuedAt, jwtProperties.refreshTokenExpiration)

        redisTemplate.setRefreshToken(userId, refreshToken)

        return TokenResponse(accessToken, refreshToken)
    }

    /**
     * access 토큰에서 사용자 Id를 파싱하는 메소드
     * @param token access token
     * @return 사용자 Id
     * @throws UnsupportedJwtException token에 subject가 없을 시 발생
     */
    private fun parseUserId(token: String): Long {
        val claims = Jwts.parser().parseJwtSignedClaims(key, token)

        val sub = claims.payload.subject ?: throw UnsupportedJwtException(token)
        return sub.toLong()
    }

    /**
     * refresh 토큰을 이용해 access 토큰 재발급, 만약 refresh 토큰의 재발급 기준 시간 이하라면 같이 재발급한다.
     * @param refreshToken refresh 토큰
     * @return 재발급된 토큰들
     * @throws IllegalArgumentException refresh 토큰이 Redis에 저장되어 있지 않을 때
     * @throws UserExpiredException 만료된 사용자일 때
     */
    fun reissueToken(refreshToken: String): TokenResponse {
        val userId =
            (
                redisTemplate.opsForValue()[refreshToken]
                    ?: throw IllegalArgumentException("비정상적인 refresh 토큰입니다.")
            ).toLong()

        try {
            checkUserNotExpired(userId)
        } catch (e: UserExpiredException) {
            redisTemplate.delete(refreshToken)
            throw e
        }

        val issuedAt = Date()
        val newRefreshToken =
            if (shouldReissueRefreshToken(
                    refreshToken,
                    issuedAt,
                )
            ) {
                Jwts
                    .builder()
                    .createRefreshToken(key, issuedAt, jwtProperties.refreshTokenExpiration)
                    .also { redisTemplate.setRefreshToken(userId, it) }
            } else {
                null
            }

        val accessToken = Jwts.builder().createAccessToken(key, userId, issuedAt, jwtProperties.accessTokenExpiration)

        return TokenResponse(accessToken, newRefreshToken)
    }

    /**
     * 사용자가 만료되었는지 확인하는 메소드
     * @param userId 사용자 id
     * @throws UserExpiredException 사용자가 만료된 사용자일 시
     */
    private fun checkUserNotExpired(userId: Long) {
        if (redisTemplate.opsForSet().isMember(RedisConstant.EXPIRED_USERS_REDIS_KEY, userId.toString()) == true) {
            log.info("만료된 사용자 입니다 : {userId : $userId}")
            throw UserExpiredException()
        }
    }

    /**
     * refresh 토큰을 재발급해야 하는지 판단하는 메소드
     * @param refreshToken refresh 토큰
     * @param issuedAt 기준 시간
     * @return 재발급 여부
     */
    private fun shouldReissueRefreshToken(
        refreshToken: String,
        issuedAt: Date,
    ): Boolean {
        try {
            val claims = Jwts.parser().parseJwtSignedClaims(key, refreshToken)
            return claims.payload.expiration.time - issuedAt.time <= jwtProperties.refreshTokenReissueExpiration
        } catch (e: ExpiredJwtException) {
            throw RefreshTokenExpiredException(refreshToken)
        }
    }

    /**
     * Refresh 토큰을 캐시에 저장하는 메소드
     * @param userId 사용자 Id
     * @param refreshToken 저장할 refresh 토큰
     */
    private fun RedisTemplate<String, String>.setRefreshToken(
        userId: Long,
        refreshToken: String,
    ) = opsForValue()
        .set(refreshToken, userId.toString(), jwtProperties.refreshTokenExpiration, TimeUnit.MILLISECONDS)
}

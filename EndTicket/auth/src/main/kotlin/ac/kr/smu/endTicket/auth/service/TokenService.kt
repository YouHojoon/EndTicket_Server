package ac.kr.smu.endTicket.auth.service

import ac.kr.smu.endTicket.auth.domain.exception.NotFoundUserException

import ac.kr.smu.endTicket.auth.ui.response.TokenResponse
import ac.kr.smu.endTicket.auth.infra.property.JWTProperties
import ac.kr.smu.endTicket.protobuf.AccessToken
import ac.kr.smu.endTicket.protobuf.TokenServiceGrpc
import ac.kr.smu.endTicket.protobuf.ValidateAccessTokenResponse

import io.grpc.StatusRuntimeException
import io.grpc.stub.StreamObserver

import io.jsonwebtoken.*
import io.jsonwebtoken.security.Keys
import io.jsonwebtoken.security.SignatureException
import net.devh.boot.grpc.server.service.GrpcService
import org.springframework.data.redis.core.RedisTemplate

import org.springframework.stereotype.Service
import java.util.Date
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
    private val jwtProperties: JWTProperties
): TokenServiceGrpc.TokenServiceImplBase(){
    /**
     * JWT를 서명하기 위한 key
     */
    private val key = Keys.hmacShaKeyFor(jwtProperties.secret.toByteArray())

    /**
     * access 토큰을 검증하는 메소드
     * @param request 검증할 AccessToken
     * @param responseObserver 결과를 전달받을 옵저버
     */
    override fun validateAccessToken(request: AccessToken, responseObserver: StreamObserver<ValidateAccessTokenResponse>) {
        try {
            val userID = parseUserID(request.token.split(" ").last())
            responseObserver.onNext(
                createValidateAccessTokenResponse(userID,200)
            )
        } catch (e: ExpiredJwtException) {
           responseObserver.onNext(
               createValidateAccessTokenResponse(status = 401, message = "토큰이 만료됐습니다.")
           )
        }
        catch (e: SignatureException){
            responseObserver.onNext(
                createValidateAccessTokenResponse(status = 400, message = "토큰 서명 검증에 실패했습니다.")
            )
        }
        catch (e: UnsupportedJwtException){
            responseObserver.onNext(
                createValidateAccessTokenResponse(status = 400, message = "올바르지 않은 토큰입니다.")
            )
        }
        catch (e: StatusRuntimeException){
            responseObserver.onNext(
                createValidateAccessTokenResponse( status = 500, message = e.message)
            )
        }
        responseObserver.onCompleted()
    }

    /**
     * 토큰 생성 기능
     * @param userID 사용자 번호
     * @return JWT 토큰 발급
     */
    @Throws(NotFoundUserException::class)
    fun createAccessAndRefreshToken(userID: Long): TokenResponse{
        val issuedAt = Date()
        val accessToken = createAccessToken(userID, issuedAt)
        val refreshToken = createRefreshToken(issuedAt)

        redisTemplate.setRefreshToken(userID, refreshToken)

        return TokenResponse(accessToken, refreshToken)
    }

    /**
     * access 토큰에서 사용자 ID를 파싱하는 메소드
     * @param token access token
     * @return 사용자 ID
     * @throws UnsupportedJwtException token에 subject가 없을 시 발생
     */
    fun parseUserID(token: String): Long{
        val claims = Jwts.parser().parseJWTSignedClaims(token)

        val sub = claims.payload.subject ?: throw UnsupportedJwtException(token)
        return sub.toLong()
    }

    /**
     * refresh 토큰을 이용해 access 토큰 재발급, 만약 refresh 토큰의 재발급 기준 시간 이하라면 같이 재발급한다.
     * @param refreshToken refresh 토큰
     * @return 재발급된 토큰들
     * @throws IllegalStateException refresh 토큰이 Redis에 저장되어 있지 않을 때
     */
    @Throws(IllegalStateException::class)
    fun reissueToken(refreshToken: String): TokenResponse{
        val userID = redisTemplate.opsForValue().get(refreshToken)

        checkNotNull(userID){
            "비정상적인 Refresh 토큰입니다."
        }

        val issuedAt = Date()
        val newRefreshToken = if (shouldReissueRefreshToken(refreshToken, issuedAt)) createRefreshToken(issuedAt) else null
        val accessToken = createAccessToken(userID.toLong(), issuedAt)

        return TokenResponse(accessToken, newRefreshToken)
    }

    /**
     * refresh 토큰을 재발급해야 하는지 판단하는 메소드
     * @param refreshToken refresh 토큰
     * @param issuedAt 기준 시간
     * @return 재발급 여부
     */
    private fun shouldReissueRefreshToken(refreshToken: String, issuedAt: Date): Boolean{
        val claims = Jwts.parser().parseJWTSignedClaims(refreshToken)
        return claims.payload.expiration.time - issuedAt.time <= jwtProperties.refreshTokenReissueExpiration
    }
    /**
     * access 토큰 생성
     * @param userID 사용자 ID
     * @param issuedAt 생성 시간
     * @return access 토큰 반환
     */
    private fun createAccessToken(userID: Long, issuedAt: Date): String{
        return Jwts
            .builder()
            .signWith(key)
            .issuedAt(issuedAt)
            .subject(userID.toString())
            .expiration(Date(issuedAt.time + jwtProperties.accessTokenExpiration))
            .compact()
    }

    /**
     * refresh 토큰 생성
     * @param issuedAt 생성 시간
     * @return refresh 토큰 반환
     */
    private fun createRefreshToken(issuedAt: Date): String{
        return Jwts
            .builder()
            .signWith(key)
            .issuedAt(issuedAt)
            .expiration(Date(issuedAt.time + jwtProperties.refreshTokenExpiration))
            .compact()
    }

    /**
     * JWT 토큰에서 Claims 반환
     * @param token JWT 토큰
     * @return 파싱된 Claims
     */
    private fun JwtParserBuilder.parseJWTSignedClaims(token: String): Jws<Claims>{
        return this
            .verifyWith(key)
            .build()
            .parseSignedClaims(token)
    }

    /**
     * Refresh 토큰을 캐시에 저장하는 메소드
     * @param userID 사용자 ID
     * @param refreshToken 저장할 refresh 토큰
     */
    private fun RedisTemplate<String,String>.setRefreshToken(userID: Long, refreshToken: String){
        this.opsForValue().set(refreshToken,userID.toString(), jwtProperties.refreshTokenExpiration, TimeUnit.MILLISECONDS)
    }

    /**
     * gRPC를 통해 반환될 응답을 생성하는 메소드
     * @param userID 토큰에서 파싱한 사용자 ID
     * @param status 상태, HttpStatusCode와 대응된다.
     * @param message 에러 발생 시 메시지
     */
    private fun createValidateAccessTokenResponse(userID: Long? = null, status: Int, message: String? = null): ValidateAccessTokenResponse{
        var response = ValidateAccessTokenResponse
            .newBuilder()
            .setStatus(status)

        response.setUserID(userID ?: -1)
        if (message != null)
            response.setMessage(message)

        return response.build()
    }

}
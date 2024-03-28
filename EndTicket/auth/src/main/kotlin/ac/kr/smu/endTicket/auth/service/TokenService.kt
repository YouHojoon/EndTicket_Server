package ac.kr.smu.endTicket.auth.service

import ac.kr.smu.endTicket.auth.domain.exception.UserNotFoundException

import ac.kr.smu.endTicket.auth.ui.response.CreateTokenResponse
import ac.kr.smu.endTicket.auth.ui.response.ReissueTokenResponse
import ac.kr.smu.endTicket.infra.config.JWTProperties

import io.jsonwebtoken.*
import io.jsonwebtoken.security.Keys
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
class TokenService(
    private val redisTemplate: RedisTemplate<String, String>,
    private val jwtProperties: JWTProperties
) {
    /**
     * JWT를 서명하기 위한 key
     */
    private val key = Keys.hmacShaKeyFor(jwtProperties.secret.toByteArray())

    /**
     * 토큰 생성 기능
     * @param userID 사용자 번호
     * @return JWT 토큰 발급
     */
    @Throws(UserNotFoundException::class)
    fun createAccessAndRefreshToken(userID: Long): CreateTokenResponse{
        val issuedAt = Date()
        val accessToken = createAccessToken(userID, issuedAt)
        val refreshToken = createRefreshToken(issuedAt)

        redisTemplate.setRefreshToken(userID, refreshToken)

        return CreateTokenResponse(accessToken, refreshToken)
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
     * @throws IllegalArgumentException refresh 토큰이 Redis에 저장되어 있지 않을 때
     */
    @Throws(IllegalArgumentException::class)
    fun reissueToken(refreshToken: String): ReissueTokenResponse{
        val userID = redisTemplate.opsForValue().get(refreshToken)

        requireNotNull(userID){
            "비정상적인 Refresh 토큰입니다."
        }

        val issuedAt = Date()
        val newRefreshToken = if (shouldReissueRefreshToken(refreshToken, issuedAt)) createRefreshToken(issuedAt) else null
        val accessToken = createAccessToken(userID.toLong(), issuedAt)

        return ReissueTokenResponse(accessToken, newRefreshToken)
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

    private fun RedisTemplate<String,String>.setRefreshToken(userID: Long, refreshToken: String){
        this.opsForValue().set(refreshToken,userID.toString(), jwtProperties.refreshTokenExpiration, TimeUnit.MILLISECONDS)
    }
}
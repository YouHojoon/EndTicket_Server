package ac.kr.smu.endTicket.auth.domain.model

import io.jsonwebtoken.Jwts
import io.swagger.v3.oas.annotations.media.Schema

import java.security.Key
import java.util.Date

/**
 * JWT 토큰을 추상화한 클래스
 * @property userId 사용자 ID
 * @property key 서명할 key
 * @property accessTokenExpiration access 토큰 만료 시간
 * @property refreshTokenExpiration refresh 토큰 만료 시간
 *
 */
@Schema(description = "JWT 토큰")
class JWTToken(userID: Long, key: Key, accessTokenExpiration: Long, refreshTokenExpiration: Long){
    /**
     * JWT 방식의 access token
     */
    @Schema(description = "access 토큰", example = "eyJhbGciOiJIUzM4NCJ9.eyJpYXQiOjE3MDc4NDA2OTIsInN1YiI6IjAiLCJleHAiOjE3MDc4NDA2OTN9.FQxA4a90uB12IwvUDMcXMSAmvF6ph-3whY9wVaqCCcCul8xO2ZLWUPtoMw1Vcieu")
    val accessToken: String
    /**
     * JWT 방식의 refresh token
     */
    @Schema(description = "refresh 토큰", example = "eyJhbGciOiJIUzM4NCJ9.eyJpYXQiOjE3MDc4NDA2OTIsInN1YiI6IjAiLCJleHAiOjE3MDc4NDMyODR9.9PdC41rjzAKsAFil9cfbNtTDmqz-EJjgdI6_cPUBuS9mMOqNASVAt6shxh2PQ93m")
    val refreshToken: String

    init {
        val now = Date()

        val builder = Jwts
            .builder()
            .issuedAt(now)
            .signWith(key)

        accessToken = builder
            .subject(userID.toString())
            .expiration(Date(now.time + accessTokenExpiration))
            .compact()
        refreshToken = builder
            .expiration(Date(now.time + refreshTokenExpiration))
            .compact()
    }
}
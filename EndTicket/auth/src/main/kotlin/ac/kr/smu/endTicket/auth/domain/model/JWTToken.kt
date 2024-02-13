package ac.kr.smu.endTicket.auth.domain.model

import io.jsonwebtoken.Jwts

import java.security.Key
import java.util.Date

/**
 * JWT 토큰을 추상화한 클래스
 * @property userId 사용자 ID
 * @property key 서명할 key
 * @property accessTokenExpiration access 토큰 만료 시간
 * @property refreshTokenExpiration refresh 토큰 만료 시간
 */
class JWTToken(userID: Long, key: Key, accessTokenExpiration: Long, refreshTokenExpiration: Long){
    /**
     * JWT 방식의 access token
     */
    val accessToken: String
    /**
     * JWT 방식의 refresh token
     */
    val refreshToken: String

    init {
        val now = Date()

        val builder = Jwts
            .builder()
            .issuedAt(now)
            .subject(userID.toString())
            .signWith(key)

        accessToken = builder
            .expiration(Date(now.time + accessTokenExpiration))
            .compact()
        refreshToken = builder
            .expiration(Date(now.time + refreshTokenExpiration))
            .compact()
    }
}
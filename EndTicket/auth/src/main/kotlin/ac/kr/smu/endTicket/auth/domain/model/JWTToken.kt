package ac.kr.smu.endTicket.auth.domain.model

import io.jsonwebtoken.Jwts

import java.security.Key
import java.util.Date

/**
 * JWT 토큰을 추상화한 클래스
 * @property userId 사용자 ID
 * @property key 서명할 key
 */
class JWTToken(userID: Long, key: Key){
    val accessToken: String
    val refreshToken: String

    private val ACCESS_TOKEN_EXPIRATION = 60 * 30
    private val REFRESH_TOKEN_EXPIRATION = 60 * 60 * 24 * 30

    init {
        val builder = Jwts
            .builder()
            .subject(userID.toString())
            .signWith(key)
        val now = Date()

        accessToken = builder.expiration(Date(now.time + ACCESS_TOKEN_EXPIRATION)).compact()
        refreshToken = builder.expiration(Date(now.time + REFRESH_TOKEN_EXPIRATION)).compact()
    }
}
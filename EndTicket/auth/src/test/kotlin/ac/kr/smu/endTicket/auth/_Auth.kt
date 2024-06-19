package ac.kr.smu.endTicket.auth

import ac.kr.smu.endTicket.auth.domain.model.SocialType
import io.jsonwebtoken.JwtBuilder
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import java.util.Date


const val AUTHORIZATION_CODE = "1"
const val SOCIAL_USER_NUMBER = "1"
const val USER_ID = 1L
const val ACCESS_TOKEN = "ac/kr/smu/endticket/common"
const val REFRESH_TOKEN = "r"
const val ID_TOKEN = "i"

val SOCIAL_TYPE = SocialType.KAKAO


fun JwtBuilder.createMockRefreshToken(secret: String, expirationTime: Long): String{
    val issuedAt = Date()

    return Jwts
    .builder()
    .signWith(
        Keys.hmacShaKeyFor(secret.toByteArray())
    )
    .issuedAt(issuedAt)
    .expiration(Date(issuedAt.time + expirationTime))
    .compact()
}


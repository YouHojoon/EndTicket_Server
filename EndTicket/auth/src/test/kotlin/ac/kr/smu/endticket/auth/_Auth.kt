package ac.kr.smu.endticket.auth

import io.jsonwebtoken.JwtBuilder
import io.jsonwebtoken.security.Keys
import java.util.*

fun JwtBuilder.createMockRefreshToken(
    secret: String,
    expirationTime: Long,
): String {
    val issuedAt = Date()

    return signWith(
        Keys.hmacShaKeyFor(secret.toByteArray()),
    ).issuedAt(issuedAt)
        .expiration(Date(issuedAt.time + expirationTime))
        .compact()
}

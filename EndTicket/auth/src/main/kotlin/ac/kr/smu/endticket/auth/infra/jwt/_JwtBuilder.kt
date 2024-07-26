package ac.kr.smu.endticket.auth.infra.jwt

import io.jsonwebtoken.JwtBuilder
import io.jsonwebtoken.Jwts
import java.security.Key
import java.util.*

/**
 * access 토큰 생성
 * @param key 서명할 키
 * @param userId 사용자 Id
 * @param issuedAt 생성 시간
 * @param expiration 유효 시간
 * @return access 토큰 반환
 */
fun JwtBuilder.createAccessToken(
    key: Key,
    userId: Long,
    issuedAt: Date,
    expiration: Long,
) =
    signWith(key)
    .issuedAt(issuedAt)
    .subject(userId.toString())
    .expiration(Date(issuedAt.time + expiration))
    .compact()

/**
 * refresh 토큰 생성
 * @param key 서명할 키
 * @param issuedAt 생성 시간
 * @param expiration 유효 시간
 * @return refresh 토큰 반환
 */
fun JwtBuilder.createRefreshToken(
    key: Key,
    issuedAt: Date,
    expiration: Long,
): String =
    Jwts
        .builder()
        .signWith(key)
        .issuedAt(issuedAt)
        .expiration(Date(issuedAt.time + expiration))
        .compact()
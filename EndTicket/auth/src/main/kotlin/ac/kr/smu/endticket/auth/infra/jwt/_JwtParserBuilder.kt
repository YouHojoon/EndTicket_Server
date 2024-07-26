package ac.kr.smu.endticket.auth.infra.jwt

import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jws
import io.jsonwebtoken.JwtParserBuilder
import javax.crypto.SecretKey

/**
 * JWT 토큰에서 Claims 반환
 * @param secretKey 서명에 사용한 키
 * @param token JWT 토큰
 * @return 파싱된 Claims
 */
fun JwtParserBuilder.parseJwtSignedClaims(
    secretKey: SecretKey,
    token: String,
): Jws<Claims> =
    verifyWith(secretKey)
        .build()
        .parseSignedClaims(token)

package ac.kr.smu.endticket.auth.infra.security

import org.springframework.security.oauth2.server.authorization.OAuth2Authorization
import java.time.Instant

/**
 * OAuth2Authorization을 DB에 저장하기 위한 객체
 * @property principalName authorization의 principal 이름
 * @property authorizationGrantType authorization의 인증 타입
 * @property registeredClientId 인증된 RegisteredClient의 id
 * @property refreshTokenValue refresh 토큰
 * @property refreshTokenIssuedAt refresh 토큰의 발급 시간
 * @property refreshTokenExpiresAt refresh 토큰의 만료 시간
 */
class OAuth2AuthorizationRecord private constructor(
    val principalName: String,
    val authorizationGrantType: String,
    val registeredClientId: String,
    val refreshTokenValue: String?,
    val refreshTokenIssuedAt: Instant?,
    val refreshTokenExpiresAt: Instant?,
) {
    companion object {
        fun from(authorization: OAuth2Authorization) =
            OAuth2AuthorizationRecord(
                registeredClientId = authorization.registeredClientId,
                authorizationGrantType = authorization.authorizationGrantType.value,
                principalName = authorization.principalName,
                refreshTokenValue = authorization.refreshToken?.token?.tokenValue,
                refreshTokenIssuedAt = authorization.refreshToken?.token?.issuedAt,
                refreshTokenExpiresAt = authorization.refreshToken?.token?.expiresAt,
            )
    }
}

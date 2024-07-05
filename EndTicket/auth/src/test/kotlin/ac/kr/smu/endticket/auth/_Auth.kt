package ac.kr.smu.endticket.auth

import ac.kr.smu.endTicket.auth.domain.service.OAuthService
import ac.kr.smu.endTicket.auth.infra.oauth2.OAuth2TokenResponse
import io.jsonwebtoken.JwtBuilder
import io.jsonwebtoken.security.Keys
import org.mockito.Mockito
import java.util.Date

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

fun mockOauthService(service: OAuthService) {
    Mockito
        .`when`(service.oAuth(AuthTestParameters.SOCIAL_TYPE, AuthTestParameters.AUTHORIZATION_CODE))
        .thenReturn(
            OAuth2TokenResponse(
                accessToken = AuthTestParameters.ACCESS_TOKEN,
                refreshToken = AuthTestParameters.REFRESH_TOKEN,
                idToken = AuthTestParameters.ID_TOKEN,
                expiresIn = 1,
                tokenType = "t",
                scope = "",
                refreshTokenExpiresIn = "",
            ),
        )

    Mockito
        .`when`(service.parseSocialUserNumber(AuthTestParameters.SOCIAL_TYPE, AuthTestParameters.ID_TOKEN))
        .thenReturn(AuthTestParameters.SOCIAL_USER_NUMBER)
}

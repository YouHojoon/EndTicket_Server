package ac.kr.smu.endticket.auth

import ac.kr.smu.endTicket.auth.domain.exception.UserExpiredException
import ac.kr.smu.endTicket.auth.domain.service.OAuth2Service
import ac.kr.smu.endTicket.auth.infra.oauth2.OAuth2TokenResponse
import io.jsonwebtoken.JwtBuilder
import io.jsonwebtoken.security.Keys
import io.kotest.core.spec.style.scopes.DescribeSpecContainerScope
import io.mockk.every
import org.mockito.Mockito
import java.util.Date
import java.util.concurrent.CompletableFuture

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

fun DescribeSpecContainerScope.mockOAuth2Service(oauth2Service: OAuth2Service){
    beforeTest {
        every {
            oauth2Service.oAuth(
                AuthTestParameters.SOCIAL_TYPE,
                AuthTestParameters.AUTHORIZATION_CODE,
            )
        } returns
                OAuth2TokenResponse(
                    accessToken = AuthTestParameters.ACCESS_TOKEN,
                    refreshToken = AuthTestParameters.REFRESH_TOKEN,
                    idToken = AuthTestParameters.ID_TOKEN,
                    expiresIn = 1,
                    tokenType = "t",
                    scope = "",
                    refreshTokenExpiresIn = "",
                )

        every {
            oauth2Service.parseSocialUserNumber(
                AuthTestParameters.SOCIAL_TYPE,
                AuthTestParameters.ID_TOKEN,
            )
        } returns AuthTestParameters.SOCIAL_USER_NUMBER
    }
}

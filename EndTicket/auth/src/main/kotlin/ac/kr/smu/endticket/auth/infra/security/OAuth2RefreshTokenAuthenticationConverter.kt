package ac.kr.smu.endticket.auth.infra.security

import jakarta.servlet.http.HttpServletRequest
import org.jetbrains.annotations.NotNull
import org.springframework.security.core.Authentication
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2ClientAuthenticationToken
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2RefreshTokenAuthenticationToken
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository
import org.springframework.security.oauth2.server.authorization.web.authentication.OAuth2RefreshTokenAuthenticationConverter
import org.springframework.security.web.authentication.AuthenticationConverter

/**
 * refresh 토큰 발급을 위한 인증 객체 Converter
 * @param lazyRegisteredClientRepository RegisteredClientRepository의 initializer
 */
class OAuth2RefreshTokenAuthenticationConverter(
    lazyRegisteredClientRepository: Lazy<RegisteredClientRepository>
) : AuthenticationConverter {
    private val converter = OAuth2RefreshTokenAuthenticationConverter()
    private val registeredClientRepository: RegisteredClientRepository by lazyRegisteredClientRepository

    override fun convert(
        @NotNull request: HttpServletRequest,
    ): Authentication? {
        val authentication =
            converter.convert(request)?.let {
                it as OAuth2RefreshTokenAuthenticationToken
            } ?: return null

        val client = registeredClientRepository.findEndticketClient()
        val principal =
            OAuth2ClientAuthenticationToken(
                client,
                client.clientAuthenticationMethods.first(),
                null,
            )

        return OAuth2RefreshTokenAuthenticationToken(
            authentication.refreshToken,
            principal,
            authentication.scopes,
            authentication.additionalParameters,
        )
    }
}

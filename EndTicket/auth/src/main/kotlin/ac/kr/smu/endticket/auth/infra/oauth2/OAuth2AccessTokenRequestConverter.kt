package ac.kr.smu.endticket.auth.infra.oauth2

import jakarta.servlet.http.HttpServletRequest
import org.jetbrains.annotations.NotNull
import org.slf4j.LoggerFactory
import org.springframework.core.convert.converter.Converter
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.core.Authentication
import org.springframework.security.oauth2.client.authentication.OAuth2LoginAuthenticationToken
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository
import org.springframework.security.oauth2.core.AuthorizationGrantType
import org.springframework.security.oauth2.core.OAuth2AuthenticationException
import org.springframework.security.oauth2.core.OAuth2ErrorCodes
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationExchange
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationResponse
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames
import org.springframework.security.oauth2.core.oidc.OidcScopes
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2AuthorizationCodeAuthenticationToken
import org.springframework.security.web.authentication.AuthenticationConverter
import org.springframework.security.web.util.matcher.AntPathRequestMatcher

/**
 * OAuth2 Token 요청을 변환해주는 객체
 * @property clientRegistrationRepository OAuth2 로그인을 수행한 client를 조회하는 클래스
 */
class OAuth2AccessTokenRequestConverter(
    private val clientRegistrationRepository: ClientRegistrationRepository,
    private val authenticationManager: AuthenticationManager,
    tokenEndpoint: String,
    authenticationConverterInitializer: () -> Converter<OAuth2LoginAuthenticationToken, OAuth2AuthorizationCodeAuthenticationToken>,
) : AuthenticationConverter {
    private val matcher = AntPathRequestMatcher("$tokenEndpoint/{$REGISTRATION_ID_URI_VARIABLE_NAME}")
    private val log = LoggerFactory.getLogger(OAuth2AccessTokenRequestConverter::class.java)
    private val authenticationConverter: Converter<OAuth2LoginAuthenticationToken, OAuth2AuthorizationCodeAuthenticationToken> by lazy(
        authenticationConverterInitializer,
    )

    private companion object {
        private const val REGISTRATION_ID_URI_VARIABLE_NAME = "registrationId"
    }

    override fun convert(
        @NotNull request: HttpServletRequest,
    ): Authentication? {
        if (!matcher.matches(request) ||
            !request
                .getParameter(OAuth2ParameterNames.GRANT_TYPE)
                .equals(AuthorizationGrantType.AUTHORIZATION_CODE.value)
        ) {
            return null
        }

        val codes = request.getParameterValues(OAuth2ParameterNames.CODE)
        if (codes.size != 1) {
            throw OAuth2EndpointUtils.parameterError(OAuth2ErrorCodes.INVALID_REQUEST, OAuth2ParameterNames.CODE)
        }

        val registrationId = matcher.matcher(request).variables[REGISTRATION_ID_URI_VARIABLE_NAME]
        if (registrationId.isNullOrBlank()) {
            throw OAuth2EndpointUtils.parameterError(
                OAuth2ErrorCodes.INVALID_REQUEST,
                REGISTRATION_ID_URI_VARIABLE_NAME,
            )
        }

        val code = codes.first()
        val clientRegistration =
            clientRegistrationRepository.findByRegistrationId(registrationId)
                ?: throw OAuth2AuthenticationException(OAuth2ErrorCodes.INVALID_CLIENT)

        val authRequest =
            OAuth2AuthorizationRequest
                .authorizationCode()
                .clientId(clientRegistration.clientId)
                .state("")
                .authorizationUri(clientRegistration.providerDetails.authorizationUri)
                .scope(OidcScopes.OPENID)
                .build()

        val authResponse =
            OAuth2AuthorizationResponse
                .success(code)
                .state("")
                .redirectUri(clientRegistration.redirectUri)
                .build()

        val authExchange = OAuth2AuthorizationExchange(authRequest, authResponse)

        try {
            // 인증 위임
            val authResult =
                authenticationManager.authenticate(
                    OAuth2LoginAuthenticationToken(
                        clientRegistration,
                        authExchange,
                    ),
                ) as OAuth2LoginAuthenticationToken

            return authenticationConverter.convert(authResult)
        } catch (e: Exception) {
            log.error("OAuth2 인증 실패", e)
            throw e
        }
    }
}

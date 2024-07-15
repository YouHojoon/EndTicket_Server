package ac.kr.smu.endticket.auth.infra.security

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpMethod
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken
import org.springframework.security.oauth2.client.authentication.OAuth2LoginAuthenticationToken
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository
import org.springframework.security.oauth2.core.OAuth2AuthenticationException
import org.springframework.security.oauth2.core.OAuth2Error
import org.springframework.security.oauth2.core.OAuth2ErrorCodes
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationExchange
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationResponse
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames
import org.springframework.security.oauth2.core.oidc.OidcScopes
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter
import org.springframework.security.web.util.matcher.AntPathRequestMatcher

/**
 * 토큰 생성 전의 인증을 처리하는 필터
 * SNS의 OAuth2 서비스를 통해 인증한다.
 * @property clientRegistrationRepository OAuth2 서비스 조회를 위한 저장소
 * @property authenticationManager Authentication의 생성을 담당하는 객체
 */
class OAuth2TokenRequestAuthenticationFilter(
    private val clientRegistrationRepository: ClientRegistrationRepository,
    private val authenticationManager: AuthenticationManager,
) : AbstractAuthenticationProcessingFilter(AntPathRequestMatcher("/auth/token*", HttpMethod.POST.name())) {
    private val securityContextHolderStrategy = SecurityContextHolder.getContextHolderStrategy()

    private companion object {
        private const val SOCIAL_TYPE_PARAMETER_NAME = "socialType"
    }

    override fun attemptAuthentication(
        request: HttpServletRequest,
        response: HttpServletResponse,
    ): Authentication {
        val parameters = request.parameterMap
        val codes = parameters[OAuth2ParameterNames.CODE]

        if (codes == null || codes.size != 1) {
            throw parameterError(OAuth2ErrorCodes.INVALID_REQUEST, OAuth2ParameterNames.CODE)
        }

        val socialTypes = parameters[SOCIAL_TYPE_PARAMETER_NAME]
        if (socialTypes == null || socialTypes.size != 1) {
            throw parameterError(OAuth2ErrorCodes.INVALID_REQUEST, SOCIAL_TYPE_PARAMETER_NAME)
        }

        val code = codes.first()
        val socialType = socialTypes.first()

        val clientRegistration =
            clientRegistrationRepository.findByRegistrationId(socialType)
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
            val authResult =
                authenticationManager.authenticate(
                    OAuth2LoginAuthenticationToken(
                        clientRegistration,
                        authExchange,
                    ),
                ) as OAuth2LoginAuthenticationToken

            return OAuth2AuthenticationToken(
                authResult.principal,
                authResult.authorities,
                authResult.clientRegistration.registrationId,
            )
        } catch (e: Exception) {
            logger.error("OAuth2 인증 실패", e)
            throw e
        }
    }

    override fun successfulAuthentication(
        request: HttpServletRequest,
        response: HttpServletResponse,
        chain: FilterChain,
        authResult: Authentication,
    ) {
        val context = securityContextHolderStrategy.createEmptyContext()
        context.authentication = authResult
        this.securityContextHolderStrategy.context = context

        chain.doFilter(request, response)
    }

    private fun parameterError(
        errorCode: String?,
        parameterName: String,
        errorUri: String? = null,
    ): OAuth2AuthenticationException {
        val error = OAuth2Error(errorCode, "OAuth 2.0 Parameter: $parameterName", errorUri)
        return OAuth2AuthenticationException(error)
    }
}

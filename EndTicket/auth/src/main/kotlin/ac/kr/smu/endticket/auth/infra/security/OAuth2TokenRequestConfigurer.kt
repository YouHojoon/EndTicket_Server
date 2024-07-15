package ac.kr.smu.endticket.auth.infra.security

import ac.kr.smu.endticket.auth.service.UserService
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer
import org.springframework.security.oauth2.client.endpoint.DefaultAuthorizationCodeTokenResponseClient
import org.springframework.security.oauth2.client.oidc.authentication.OidcAuthorizationCodeAuthenticationProvider
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository
import org.springframework.security.oauth2.client.web.OAuth2LoginAuthenticationFilter

/**
 * 토큰 요청의 인증에 대한 설정을 하는 클래스
 * @property clientRegistrationRepository OAuth2 서비스
 * @property userService 사용자 서비스 클래스
 */
class OAuth2TokenRequestConfigurer(
    private val clientRegistrationRepository: ClientRegistrationRepository,
    private val userService: UserService,
) : AbstractHttpConfigurer<OAuth2TokenRequestConfigurer, HttpSecurity>() {
    override fun init(builder: HttpSecurity) {
        builder.setSharedObject(ClientRegistrationRepository::class.java, clientRegistrationRepository)
        super.init(builder)
    }

    override fun configure(builder: HttpSecurity) {
        val authenticationManager = builder.getSharedObject(AuthenticationManager::class.java)
        val provider =
            OidcAuthorizationCodeAuthenticationProvider(
                DefaultAuthorizationCodeTokenResponseClient(),
                OAuth2UserServiceImpl(userService),
            )

        builder.authenticationProvider(postProcess(provider))
        builder.addFilterBefore(
            OAuth2TokenRequestAuthenticationFilter(
                clientRegistrationRepository,
                authenticationManager,
            ).also {
                it.setSecurityContextHolderStrategy(getSecurityContextHolderStrategy())
            },
            OAuth2LoginAuthenticationFilter::class.java,
        )

        super.configure(builder)
    }
}

package ac.kr.smu.endTicket


import ac.kr.smu.endTicket.auth.domain.service.OAuthService
import ac.kr.smu.endTicket.auth.infra.oauth2.filter.OAuth2AuthorizationFilter
import ac.kr.smu.endTicket.auth.infra.oauth2.filter.OAuth2ErrorHandlerFilter
import org.springframework.context.annotation.Bean
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.config.annotation.web.invoke
import org.springframework.security.oauth2.client.web.OAuth2LoginAuthenticationFilter
import ac.kr.smu.endticket.common.security.baseConfig
import org.springframework.boot.test.context.TestConfiguration

@TestConfiguration
@EnableWebSecurity
class SecurityTestConfig(
    private val oauthService: OAuthService,
) {
    @Bean
    fun filterChain(http: HttpSecurity): SecurityFilterChain{
        http{
            baseConfig()
            authorizeRequests {
                authorize("/auth/reissue-token",permitAll)
                authorize("/oauth/**",permitAll)
                authorize(anyRequest, authenticated)
            }

            addFilterBefore<OAuth2LoginAuthenticationFilter>(OAuth2AuthorizationFilter(oauthService))
            addFilterBefore<OAuth2AuthorizationFilter>(OAuth2ErrorHandlerFilter())
        }

        return http.build()
    }

}
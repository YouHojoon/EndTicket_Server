package ac.kr.smu.endTicket.infra.config


import ac.kr.smu.endTicket.auth.domain.service.OAuthService
import ac.kr.smu.endTicket.infra.oAuth2.filter.CustomOAuth2AuthorizationRequestRedirectFilter
import ac.kr.smu.endTicket.infra.oAuth2.filter.OAuth2ErrorHandlerFilter
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.boot.autoconfigure.security.oauth2.client.OAuth2ClientProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.config.annotation.web.invoke
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.core.Authentication
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestRedirectFilter
import org.springframework.security.oauth2.client.web.OAuth2LoginAuthenticationFilter
import org.springframework.security.web.authentication.AuthenticationSuccessHandler
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter

@Configuration
@EnableWebSecurity
class SecurityConfig(
    private val oAuthService: OAuthService
) {
    @Bean
    fun filterChain(http: HttpSecurity): SecurityFilterChain{
        http{
            formLogin { disable() }
            csrf { disable() }
            sessionManagement {
                sessionCreationPolicy = SessionCreationPolicy.STATELESS
            }
            authorizeRequests {
                authorize("/oauth/**", permitAll)
                authorize(anyRequest, authenticated)
            }
            addFilterBefore<OAuth2LoginAuthenticationFilter>(CustomOAuth2AuthorizationRequestRedirectFilter(oAuthService))
            addFilterBefore<CustomOAuth2AuthorizationRequestRedirectFilter>(OAuth2ErrorHandlerFilter())
        }

        return http.build()
    }

}
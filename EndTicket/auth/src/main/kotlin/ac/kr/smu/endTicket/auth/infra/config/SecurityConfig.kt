package ac.kr.smu.endTicket.auth.infra.config


import ac.kr.smu.endTicket.auth.domain.service.OAuthService
import ac.kr.smu.endTicket.auth.infra.oAuth2.filter.OAuth2AuthorizationFilter
import ac.kr.smu.endTicket.auth.infra.oAuth2.filter.OAuth2ErrorHandlerFilter
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.config.annotation.web.invoke
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.oauth2.client.web.OAuth2LoginAuthenticationFilter
import org.springframework.security.web.AuthenticationEntryPoint
import ac.kr.smu.endTicket.response.ExceptionResponse
import ac.kr.smu.endTicket.security.baseConfig
import ac.kr.smu.endTicket.security.baseExceptionHandling
import ac.kr.smu.endTicket.security.configLogin
import ac.kr.smu.endTicket.security.configSwaggerRequestPermitAll

@Configuration
@EnableWebSecurity
class SecurityConfig(
    private val oAuthService: OAuthService
) {
    @Bean
    fun filterChain(http: HttpSecurity): SecurityFilterChain{
        http{
            baseConfig()

            authorizeRequests {
                authorize("/oauth/**",permitAll)
                authorize("/auth/reissue-token", permitAll)
                authorize(anyRequest, authenticated)
            }

            addFilterBefore<OAuth2LoginAuthenticationFilter>(OAuth2AuthorizationFilter(oAuthService))
            addFilterBefore<OAuth2AuthorizationFilter>(OAuth2ErrorHandlerFilter())
        }

        return http.build()
    }

}
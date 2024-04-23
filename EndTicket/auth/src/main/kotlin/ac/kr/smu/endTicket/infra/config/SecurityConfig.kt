package ac.kr.smu.endTicket.infra.config


import ErrorResponse
import ac.kr.smu.endTicket.auth.domain.service.OAuthService
import ac.kr.smu.endTicket.auth.service.TokenService
import ac.kr.smu.endTicket.infra.OAuth2.filter.OAuth2AuthorizationFilter
import ac.kr.smu.endTicket.infra.OAuth2.filter.OAuth2ErrorHandlerFilter
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
                authorize("/docs/**", permitAll)
                authorize("/swagger-ui/**",permitAll)
                authorize("/api-docs/**",permitAll)
                authorize("/oauth/**",permitAll)
                authorize("/auth/reissueToken", permitAll)
                authorize(anyRequest, authenticated)
            }

            exceptionHandling {
                authenticationEntryPoint = AuthenticationEntryPoint { _, response, e ->
                    response.contentType = MediaType.APPLICATION_JSON_VALUE
                    response.status = HttpStatus.UNAUTHORIZED.value()
                    response.characterEncoding = "UTF-8"
                    response.writer.write(ObjectMapper().writeValueAsString(
                        ErrorResponse(
                            code = HttpStatus.UNAUTHORIZED.value(),
                            message = "인증에 실패했습니다.",
                            detail = e.message
                        )
                    ))
                }
            }

            addFilterBefore<OAuth2LoginAuthenticationFilter>(OAuth2AuthorizationFilter(oAuthService))
            addFilterBefore<OAuth2AuthorizationFilter>(OAuth2ErrorHandlerFilter())
        }

        return http.build()
    }

}
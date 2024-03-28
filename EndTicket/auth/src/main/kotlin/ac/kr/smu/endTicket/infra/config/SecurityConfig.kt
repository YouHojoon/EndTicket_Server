package ac.kr.smu.endTicket.infra.config


import ac.kr.smu.endTicket.auth.domain.service.OAuthService
import ac.kr.smu.endTicket.auth.service.TokenService
import ac.kr.smu.endTicket.infra.jwt.JWTAuthenticationErrorHandlerFilter

import ac.kr.smu.endTicket.infra.jwt.JWTAuthenticationFilter
import ac.kr.smu.endTicket.infra.oAuth2.filter.OAuth2AuthorizationFilter
import ac.kr.smu.endTicket.infra.oAuth2.filter.OAuth2ErrorHandlerFilter
import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.config.annotation.web.invoke
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.oauth2.client.web.OAuth2LoginAuthenticationFilter
import org.springframework.security.web.AuthenticationEntryPoint
import org.springframework.security.web.access.AccessDeniedHandler
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import java.nio.charset.CharsetEncoder

@Configuration
@EnableWebSecurity
class SecurityConfig(
    private val oAuthService: OAuthService,
    private val tokenService: TokenService
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
                authenticationEntryPoint = AuthenticationEntryPoint { _, response, _ ->
                    response.contentType = MediaType.APPLICATION_JSON_VALUE
                    response.status = HttpStatus.UNAUTHORIZED.value()
                    response.characterEncoding = "UTF-8"
                    response.writer.write(ObjectMapper().writeValueAsString(mapOf("status" to HttpStatus.UNAUTHORIZED.value(), "message" to "인증에 실패했습니다.")))
                }
            }

            addFilterBefore<OAuth2LoginAuthenticationFilter>(OAuth2AuthorizationFilter(oAuthService))
            addFilterBefore<OAuth2AuthorizationFilter>(OAuth2ErrorHandlerFilter())
            addFilterBefore<UsernamePasswordAuthenticationFilter>(JWTAuthenticationFilter(tokenService))
            addFilterBefore<JWTAuthenticationFilter>(JWTAuthenticationErrorHandlerFilter())
        }

        return http.build()
    }

}
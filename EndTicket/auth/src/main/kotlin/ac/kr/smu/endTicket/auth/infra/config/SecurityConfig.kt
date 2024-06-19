package ac.kr.smu.endTicket.auth.infra.config


import ac.kr.smu.endTicket.auth.domain.service.OAuthService
import ac.kr.smu.endTicket.auth.infra.oauth2.filter.OAuth2AuthorizationFilter
import ac.kr.smu.endTicket.auth.infra.oauth2.filter.OAuth2ErrorHandlerFilter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.config.annotation.web.invoke
import org.springframework.security.oauth2.client.web.OAuth2LoginAuthenticationFilter
import ac.kr.smu.endticket.common.security.baseConfig
import jakarta.servlet.http.HttpServletRequest
import org.springframework.cloud.client.discovery.DiscoveryClient
import org.springframework.security.web.util.matcher.AntPathRequestMatcher
import org.springframework.security.web.util.matcher.IpAddressMatcher
import org.springframework.security.web.util.matcher.RequestMatcher

@Configuration
@EnableWebSecurity
class SecurityConfig(
    private val oauthService: OAuthService,
    private val discoveryClient: DiscoveryClient
) {
    @Bean
    fun filterChain(http: HttpSecurity): SecurityFilterChain{
        http{
            baseConfig()

            authorizeRequests {
                authorize("/oauth/**",permitAll)
                discoveryClient.getInstances("gateway").forEach {
                    val ipMatcher = IpAddressMatcher(it.host)
                    authorize(
                        matches = object: RequestMatcher {
                            val pathMatcher = AntPathRequestMatcher("/auth/reissue-token")
                            override fun matches(request: HttpServletRequest): Boolean {
                                return ipMatcher.matches(request) && pathMatcher.matches(request)
                            }
                        },
                        permitAll
                    )
                    authorize(ipMatcher, authenticated)
                }
                authorize(anyRequest, denyAll)
            }

            addFilterBefore<OAuth2LoginAuthenticationFilter>(OAuth2AuthorizationFilter(oauthService))
            addFilterBefore<OAuth2AuthorizationFilter>(OAuth2ErrorHandlerFilter())
        }

        return http.build()
    }

}
package ac.kr.smu.endTicket.auth.config

import ac.kr.smu.endTicket.auth.domain.service.OAuth2Service
import ac.kr.smu.endTicket.auth.infra.oauth2.filter.OAuth2AuthorizationFilter
import ac.kr.smu.endTicket.auth.infra.oauth2.filter.OAuth2ErrorHandlerFilter
import ac.kr.smu.endticket.common.security.baseConfig
import jakarta.servlet.http.HttpServletRequest
import org.springframework.cloud.client.discovery.DiscoveryClient
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.invoke
import org.springframework.security.oauth2.client.web.OAuth2LoginAuthenticationFilter
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.util.matcher.AntPathRequestMatcher
import org.springframework.security.web.util.matcher.IpAddressMatcher
import org.springframework.security.web.util.matcher.RequestMatcher

@Configuration
@EnableWebSecurity
class SecurityConfig(
    private val oauth2Service: OAuth2Service,
    private val discoveryClient: DiscoveryClient,
) {
    @Bean
    fun filterChain(http: HttpSecurity): SecurityFilterChain {
        http {
            baseConfig()

            authorizeRequests {
                authorize("/oauth/**", permitAll)
                discoveryClient.getInstances("gateway").forEach {
                    val ipMatcher = IpAddressMatcher(it.host)
                    /*
                        게이트웨이에서 오는 요청 중 토큰 재발급 제외하고 인증 필요
                     */
                    authorize(
                        matches =
                            object : RequestMatcher {
                                val pathMatcher = AntPathRequestMatcher("/auth/reissue-token")

                                override fun matches(request: HttpServletRequest): Boolean =
                                    ipMatcher.matches(request) && pathMatcher.matches(request)
                            },
                        permitAll,
                    )

                    authorize(ipMatcher, authenticated)
                }
                authorize(anyRequest, denyAll)
            }

            addFilterBefore<OAuth2LoginAuthenticationFilter>(OAuth2AuthorizationFilter(oauth2Service))
            addFilterBefore<OAuth2AuthorizationFilter>(OAuth2ErrorHandlerFilter())
        }

        return http.build()
    }
}

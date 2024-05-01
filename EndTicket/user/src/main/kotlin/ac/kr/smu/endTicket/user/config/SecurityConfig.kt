package ac.kr.smu.endTicket.user.config

import org.springframework.cloud.client.discovery.DiscoveryClient
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.config.annotation.web.invoke
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.util.matcher.IpAddressMatcher

@Configuration
@EnableWebSecurity
class SecurityConfig(
    private val discoveryClient: DiscoveryClient
) {

    @Bean
    fun filterChain(http: HttpSecurity): SecurityFilterChain{
        http{
            sessionManagement {
               sessionCreationPolicy = SessionCreationPolicy.STATELESS
            }
            csrf { disable() }
            formLogin { disable() }
            authorizeRequests {
                authorize("/docs/**", permitAll)
                authorize("/swagger-ui/**",permitAll)
                authorize("/api-docs/**",permitAll)
                discoveryClient.getInstances("gateway").forEach {
                    authorize(IpAddressMatcher(it.host), permitAll)
                }
                authorize(anyRequest, denyAll)
            }
        }

        return http.build()
    }
}
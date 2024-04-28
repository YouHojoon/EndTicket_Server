package ac.kr.smu.endTicket.infra.config

import org.springframework.cloud.client.discovery.DiscoveryClient
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.server.SecurityWebFilterChain
import org.springframework.security.config.annotation.web.invoke
import org.springframework.security.web.util.matcher.IpAddressMatcher

@EnableWebSecurity
@Configuration
class SecurityConfig(
    private val discoveryClient: DiscoveryClient
) {

    @Bean
    fun filterChain(http: HttpSecurity): SecurityFilterChain{

        http{
            authorizeHttpRequests {
                discoveryClient.getInstances("gateway").forEach {
                    authorize(IpAddressMatcher("${it.host}"), permitAll)
                }
                authorize(anyRequest, denyAll)
            }
        }

        return http.build()
    }
}
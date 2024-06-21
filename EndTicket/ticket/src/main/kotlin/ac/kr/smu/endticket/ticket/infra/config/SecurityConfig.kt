package ac.kr.smu.endticket.ticket.infra.config

import ac.kr.smu.endticket.common.security.baseConfig
import ac.kr.smu.endticket.common.security.permitOnlyWhitelistRequest
import org.springframework.cloud.client.discovery.DiscoveryClient
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.config.annotation.web.invoke

@EnableWebSecurity
@Configuration
class SecurityConfig(
    private val discoveryClient: DiscoveryClient
) {
    @Bean
    fun filterChain(http: HttpSecurity): SecurityFilterChain{
        http{
            baseConfig()
            permitOnlyWhitelistRequest(discoveryClient.getInstances("gateway").map { it.host })
        }

        return http.build()
    }
}
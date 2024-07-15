package ac.kr.smu.endticket.auth

import ac.kr.smu.endticket.common.security.baseConfig
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.invoke
import org.springframework.security.oauth2.client.web.OAuth2LoginAuthenticationFilter
import org.springframework.security.web.SecurityFilterChain

@TestConfiguration
@EnableWebSecurity
class SecurityTestConfig{
    @Bean
    fun filterChain(http: HttpSecurity): SecurityFilterChain {
        http {
            baseConfig()
            authorizeRequests {
                authorize("/auth/reissue-token", permitAll)
                authorize("/oauth/**", permitAll)
                authorize(anyRequest, authenticated)
            }
        }

        return http.build()
    }
}

package ac.kr.smu.endTicket.infra.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.config.annotation.web.invoke
import org.springframework.security.config.http.SessionCreationPolicy

@Configuration
@EnableWebSecurity
class SecurityConfig {

    @Bean
    fun filterChain(http: HttpSecurity): SecurityFilterChain{
        http{
            sessionManagement {
               sessionCreationPolicy = SessionCreationPolicy.STATELESS
            }
            csrf { disable() }
            formLogin { disable() }
            authorizeRequests {
                authorize(anyRequest, permitAll)
            }
        }

        return http.build()
    }
}
package ac.kr.smu.endTicket.infra.config

import org.springframework.boot.autoconfigure.security.oauth2.client.OAuth2ClientProperties
import org.springframework.boot.autoconfigure.security.oauth2.client.OAuth2ClientPropertiesMapper
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.config.annotation.web.invoke
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository

@Configuration
@EnableWebSecurity
@EnableConfigurationProperties(OAuth2ClientProperties::class)
class SecurityConfig(
    private val oAuthProperties: OAuth2ClientProperties
) {

    @Bean
    fun clientRegistrationRepository(): ClientRegistrationRepository{
        val registrations = OAuth2ClientPropertiesMapper(oAuthProperties).asClientRegistrations()
        return InMemoryClientRegistrationRepository(registrations)
    }
    @Bean
    fun filterChain(http: HttpSecurity): SecurityFilterChain{
        http{
            formLogin { disable() }
            csrf { disable() }
            sessionManagement {
                sessionCreationPolicy = SessionCreationPolicy.STATELESS
            }
            authorizeRequests {
                authorize(anyRequest, permitAll)
            }
        }

        return http.build()
    }
}
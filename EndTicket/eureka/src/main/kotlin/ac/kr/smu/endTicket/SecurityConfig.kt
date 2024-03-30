package ac.kr.smu.endTicket

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.invoke
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.util.matcher.IpAddressMatcher

@Configuration
@EnableWebSecurity
class SecurityConfig(
    private val whiteListProperties: WhitelistProperties
) {
    @Bean
    fun ipAddressMatcher(): List<IpAddressMatcher>{
        return whiteListProperties.addresses.map{IpAddressMatcher(it)}
    }

    @Bean
    fun filterChain(http: HttpSecurity): SecurityFilterChain{
        http{
            authorizeRequests {
                for (matcher in ipAddressMatcher()){
                    authorize(matcher, permitAll)
                }
                authorize(anyRequest, denyAll)
            }
        }

        return http.build()
    }

}
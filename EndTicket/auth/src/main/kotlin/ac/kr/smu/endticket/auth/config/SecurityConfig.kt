package ac.kr.smu.endticket.auth.config

import ac.kr.smu.endticket.auth.infra.security.OAuth2TokenRequestConfigurer
import ac.kr.smu.endticket.auth.service.UserService
import ac.kr.smu.endticket.common.security.baseConfig
import jakarta.servlet.http.HttpServletRequest
import org.springframework.boot.autoconfigure.security.oauth2.client.OAuth2ClientProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.cloud.client.discovery.DiscoveryClient
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.invoke
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.util.matcher.AntPathRequestMatcher
import org.springframework.security.web.util.matcher.IpAddressMatcher
import org.springframework.security.web.util.matcher.RequestMatcher

@Configuration
@EnableWebSecurity
@EnableConfigurationProperties(OAuth2ClientProperties::class)
class SecurityConfig {
    @Bean
    fun filterChain(
        http: HttpSecurity,
        discoveryClient: DiscoveryClient,
        clientRegistrationRepository: ClientRegistrationRepository,
        userService: UserService,
    ): SecurityFilterChain {
        http {
            baseConfig()
            authorizeRequests {
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

            formLogin { disable() }
        }

        http.apply(OAuth2TokenRequestConfigurer(clientRegistrationRepository, userService))
        return http.build()
    }
//    @Bean
//    fun jksSslBundleProperties() = JksSslBundleProperties()
//
//    @Bean
//    fun rsaKeyGenerator(properties: JksSslBundleProperties) = RSAKeyGenerator(properties)
//
//    @Bean
//    fun jwkSource(rsaKeyGenerator: RSAKeyGenerator) = JWKSet(rsaKeyGenerator.loadOrGenerateRSAKey())
//
//    @Bean
//    fun jwtDecoder(jwkSource: JWKSource<SecurityContext>) = OAuth2AuthorizationServerConfiguration.jwtDecoder(jwkSource)
//
//    @Bean
//    fun jwtEncoder(jwkSource: JWKSource<SecurityContext>) = NimbusJwtEncoder(jwkSource)
}

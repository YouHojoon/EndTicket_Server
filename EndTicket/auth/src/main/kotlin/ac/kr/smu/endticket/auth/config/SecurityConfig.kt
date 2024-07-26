package ac.kr.smu.endticket.auth.config

import ac.kr.smu.endticket.auth.infra.security.OAuth2Configurer
import ac.kr.smu.endticket.auth.service.UserService
import ac.kr.smu.endticket.common.security.baseConfig
import com.nimbusds.jose.jwk.JWKSet
import com.nimbusds.jose.jwk.RSAKey
import com.nimbusds.jose.jwk.source.ImmutableJWKSet
import com.nimbusds.jose.jwk.source.JWKSource
import com.nimbusds.jose.proc.SecurityContext
import jakarta.servlet.http.HttpServletRequest
import org.springframework.boot.autoconfigure.security.oauth2.client.OAuth2ClientProperties
import org.springframework.boot.autoconfigure.security.oauth2.server.servlet.OAuth2AuthorizationServerProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.ssl.SslBundles
import org.springframework.cloud.client.discovery.DiscoveryClient
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.invoke
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository
import org.springframework.security.oauth2.core.AuthorizationGrantType
import org.springframework.security.oauth2.core.oidc.OidcScopes
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder
import org.springframework.security.oauth2.server.authorization.client.InMemoryRegisteredClientRepository
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.util.matcher.AntPathRequestMatcher
import org.springframework.security.web.util.matcher.IpAddressMatcher
import org.springframework.security.web.util.matcher.RequestMatcher
import java.security.KeyPair
import java.security.PrivateKey
import java.security.interfaces.RSAPrivateKey
import java.security.interfaces.RSAPublicKey
import java.time.Duration
import java.util.UUID

@Configuration
@EnableWebSecurity
@EnableConfigurationProperties(
    OAuth2ClientProperties::class,
    OAuth2AuthorizationServerProperties::class,
)
class SecurityConfig {
    private companion object {
        private const val SSL_BUNDLE_NAME = "keystore"
    }

    @Bean
    fun registeredClientRepository(): RegisteredClientRepository {
        val client =
            RegisteredClient
                .withId(UUID.randomUUID().toString())
                .clientId("endticket")
                .clientSecret(UUID.randomUUID().toString())
                .redirectUri("")
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)
                .tokenSettings(
                    TokenSettings
                        .builder()
                        .accessTokenTimeToLive(Duration.ofHours(1))
                        .build()
                )
                .build()

        return InMemoryRegisteredClientRepository(client)
    }

    @Bean
    fun filterChain(
        http: HttpSecurity,
        discoveryClient: DiscoveryClient,
        clientRegistrationRepository: ClientRegistrationRepository,
        registeredClientRepository: RegisteredClientRepository,
        userService: UserService,
        authorizationServerProperties: OAuth2AuthorizationServerProperties,
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
                authorize(anyRequest, permitAll)
            }

            formLogin { disable() }
        }

        http.apply(
            OAuth2Configurer(
                clientRegistrationRepository,
                userService,
                authorizationServerProperties,
            ),
        )

        return http.build()
    }

    @Bean
    fun keyPair(bundles: SslBundles): KeyPair {
        val bundle = bundles.getBundle(SSL_BUNDLE_NAME)
        val keyStore = bundle.stores.keyStore
        val privateKey =
            keyStore.getKey(
                bundle.key.alias,
                bundle.stores.keyStorePassword.toCharArray(),
            ) as PrivateKey
        val cert = keyStore.getCertificate(bundle.key.alias)
        val publicKey = cert.publicKey

        return KeyPair(publicKey, privateKey)
    }

    @Bean
    fun jwkSource(keyPair: KeyPair): JWKSource<SecurityContext> {
        val publicKey = keyPair.public as RSAPublicKey
        val privateKey = keyPair.private as RSAPrivateKey
        val rsaKey =
            RSAKey
                .Builder(publicKey)
                .keyID(UUID.randomUUID().toString())
                .privateKey(privateKey)
                .build()

        return ImmutableJWKSet(JWKSet(rsaKey))
    }

    @Bean
    fun jwtEncoder(jwkSource: JWKSource<SecurityContext>) = NimbusJwtEncoder(jwkSource)
}

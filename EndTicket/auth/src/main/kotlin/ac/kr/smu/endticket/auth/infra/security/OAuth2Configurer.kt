package ac.kr.smu.endticket.auth.infra.security

import ac.kr.smu.endticket.auth.service.UserService
import org.jetbrains.annotations.NotNull
import org.springframework.boot.autoconfigure.security.oauth2.server.servlet.OAuth2AuthorizationServerProperties
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer
import org.springframework.security.oauth2.client.endpoint.DefaultAuthorizationCodeTokenResponseClient
import org.springframework.security.oauth2.client.oidc.authentication.OidcAuthorizationCodeAuthenticationProvider
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository
import org.springframework.security.oauth2.server.authorization.InMemoryOAuth2AuthorizationService
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configurers.OAuth2AuthorizationServerConfigurer

class OAuth2Configurer(
    private val clientRegistrationRepository: ClientRegistrationRepository,
    private val userService: UserService,
    private val authorizationServerProperties: OAuth2AuthorizationServerProperties,
    private val redisTemplate: RedisTemplate<String, Any>,
) : AbstractHttpConfigurer<OAuth2Configurer, HttpSecurity>() {
    override fun configure(
        @NotNull builder: HttpSecurity,
    ) {
        val authenticationManager = builder.getSharedObject(AuthenticationManager::class.java)
        val provider =
            OidcAuthorizationCodeAuthenticationProvider(
                DefaultAuthorizationCodeTokenResponseClient(),
                OAuth2UserServiceImpl(userService),
            )

        builder.authenticationProvider(postProcess(provider))

        // 앱에서 발급받은 Authorization code로 Access Token 발급받도록 하는 설정
        val tokenRequestConverter =
            OAuth2AccessTokenRequestConverter(
                clientRegistrationRepository,
                authenticationManager,
                authorizationServerProperties.endpoint.tokenUri,
            ) {
                OAuth2LoginAuthenticationConverter(
                    builder.getSharedObject(RegisteredClientRepository::class.java),
                    builder.getSharedObject(OAuth2AuthorizationService::class.java),
                )
            }

        // Redis에 인증 객체 저장 설정
        val lazyRegisteredClientRepository = lazy { builder.getSharedObject(RegisteredClientRepository::class.java) }
        val authorizationService =
            RedisOAuth2AuthorizationService(
                RedisRefreshTokenService(redisTemplate),
                InMemoryOAuth2AuthorizationService(),
                lazyRegisteredClientRepository,
            )
        builder.setSharedObject(
            OAuth2AuthorizationService::class.java,
            authorizationService,
        )

        OAuth2AuthorizationServerConfigurer()
            .tokenEndpoint {
                it.accessTokenRequestConverters {
                    it.add(0, tokenRequestConverter)
                    it.add(1, OAuth2RefreshTokenAuthenticationConverter(lazyRegisteredClientRepository))
                }
            }.also {
                it.init(builder)
                it.configure(builder)
            }

        super.configure(builder)
    }

    override fun init(
        @NotNull builder: HttpSecurity,
    ) {
        builder.setSharedObject(ClientRegistrationRepository::class.java, clientRegistrationRepository)
        super.init(builder)
    }
}

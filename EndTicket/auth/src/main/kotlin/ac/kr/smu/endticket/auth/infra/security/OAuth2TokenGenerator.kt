package ac.kr.smu.endticket.auth.infra.security

import org.springframework.security.oauth2.client.authentication.OAuth2AuthorizationCodeAuthenticationProvider
import org.springframework.security.oauth2.client.authentication.OAuth2LoginAuthenticationProvider
import org.springframework.security.oauth2.core.OAuth2Token
import org.springframework.security.oauth2.jwt.JwtEncoder
import org.springframework.security.oauth2.server.authorization.token.DelegatingOAuth2TokenGenerator
import org.springframework.security.oauth2.server.authorization.token.JwtGenerator
import org.springframework.security.oauth2.server.authorization.token.OAuth2AccessTokenGenerator
import org.springframework.security.oauth2.server.authorization.token.OAuth2RefreshTokenGenerator
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenContext
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenGenerator

class OAuth2TokenGenerator(
    jwtEncoder: JwtEncoder
): OAuth2TokenGenerator<OAuth2Token>{
    private val delegate: OAuth2TokenGenerator<OAuth2Token>

    init {
        val generator = JwtGenerator(jwtEncoder)
        val accessTokenGenerator = OAuth2AccessTokenGenerator()
        val refreshTokenGenerator = OAuth2RefreshTokenGenerator()

        delegate = DelegatingOAuth2TokenGenerator(generator, accessTokenGenerator, refreshTokenGenerator)
    }

    override fun generate(context: OAuth2TokenContext): OAuth2Token? = delegate.generate(context)
}
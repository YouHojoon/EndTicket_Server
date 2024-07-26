package ac.kr.smu.endticket.auth.infra.security

import org.springframework.core.convert.converter.Converter
import org.springframework.security.oauth2.client.authentication.OAuth2LoginAuthenticationToken
import org.springframework.security.oauth2.core.*
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationCode
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2AuthorizationCodeAuthenticationToken
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2ClientAuthenticationToken
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository
import java.security.Principal
import java.time.Instant

/**
 * OAuth2의 인증 토큰을 변환해주는 컨버터
 * @property registeredClientRepository RegisteredClient를 조회하기 위한 객체
 * @property authorizationService authorization code를 저장하기 위한 객체
 */
class OAuth2LoginAuthenticationConverter(
    private val registeredClientRepository: RegisteredClientRepository,
    private val authorizationService: OAuth2AuthorizationService,
) : Converter<OAuth2LoginAuthenticationToken, OAuth2AuthorizationCodeAuthenticationToken> {
    private companion object {
        private const val CLIENT_ID = "endticket"
    }

    override fun convert(source: OAuth2LoginAuthenticationToken): OAuth2AuthorizationCodeAuthenticationToken? {
        val client =
            registeredClientRepository.findByClientId(CLIENT_ID)
                ?: throw OAuth2AuthenticationException(OAuth2Error(OAuth2ErrorCodes.INVALID_CLIENT))
        val redirectUri = client.redirectUris.firstOrNull() ?: ""

        val principal =
            OAuth2ClientAuthenticationToken(
                client,
                ClientAuthenticationMethod.CLIENT_SECRET_POST,
                client.clientSecret,
            )

        val now = Instant.now()
        val authRequest =
            OAuth2AuthorizationRequest
                .authorizationCode()
                .clientId(client.clientId)
                .redirectUri(redirectUri)
                .authorizationUri(source.clientRegistration.providerDetails.authorizationUri)
                .build()

        val authorization =
            OAuth2Authorization
                .withRegisteredClient(client)
                .principalName(source.principal.name)
                .token(
                    OAuth2AuthorizationCode(
                        source.authorizationExchange.authorizationResponse.code,
                        now,
                        now.plus(client.tokenSettings.authorizationCodeTimeToLive),
                    ),
                ).authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .attributes {
                    it[OAuth2AuthorizationRequest::class.java.name] = authRequest
                    it[Principal::class.java.name] = source
                }.build()

        authorizationService.save(authorization)

        return OAuth2AuthorizationCodeAuthenticationToken(
            source.authorizationExchange.authorizationResponse.code,
            principal,
            redirectUri,
            null,
        )
    }
}

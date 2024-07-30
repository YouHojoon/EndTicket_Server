package ac.kr.smu.endticket.auth.infra.oauth2

import org.jetbrains.annotations.NotNull
import org.springframework.security.authentication.AnonymousAuthenticationToken
import org.springframework.security.core.authority.AuthorityUtils
import org.springframework.security.oauth2.core.AuthorizationGrantType
import org.springframework.security.oauth2.core.OAuth2RefreshToken
import org.springframework.security.oauth2.server.authorization.InMemoryOAuth2AuthorizationService
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository
import java.security.Principal

/**
 * Redis에 OAuth2Authorization을 저장하는 서비스
 * @property refreshTokenService refresh 토큰을 Redis에 저장하는 서비스
 * @property authorizationService refresh 토큰을 제외한 OAuth2Authorization을 저장하는 서비스
 * @param lazyRegisteredClientRepository RegisteredRepository의 initializer
 */
class RedisOAuth2AuthorizationService(
    private val refreshTokenService: RedisRefreshTokenService,
    private val authorizationService: InMemoryOAuth2AuthorizationService,
    lazyRegisteredClientRepository: Lazy<RegisteredClientRepository>,
) : OAuth2AuthorizationService {
    private val registeredClientRepository: RegisteredClientRepository by lazyRegisteredClientRepository

    override fun save(
        @NotNull authorization: OAuth2Authorization,
    ) {
        val refreshToken = authorization.refreshToken
        if (refreshToken != null) {
            refreshTokenService.save(
                refreshToken.token,
                OAuth2AuthorizationRecord.from(authorization),
            )
        } else {
            authorizationService.save(authorization)
        }
    }

    override fun remove(
        @NotNull authorization: OAuth2Authorization,
    ) {
        val refreshToken = authorization.refreshToken

        if (refreshToken != null){
            refreshTokenService.remove(refreshToken.token.tokenValue)
        }
        else{
            authorizationService.remove(authorization)
        }
    }

    override fun findById(id: String?): OAuth2Authorization? = authorizationService.findById(id)

    override fun findByToken(
        @NotNull token: String,
        tokenType: OAuth2TokenType?,
    ): OAuth2Authorization? {
        if (tokenType == OAuth2TokenType.REFRESH_TOKEN) {
            return refreshTokenService.find(token)?.let {
                convertRecordToAuthorization(it)
            }
        } else {
            return authorizationService.findByToken(token, tokenType)
        }
    }

    private fun convertRecordToAuthorization(entity: OAuth2AuthorizationRecord): OAuth2Authorization {
        val client = registeredClientRepository.findById(entity.registeredClientId)
        val principal =
            AnonymousAuthenticationToken(
                entity.principalName,
                entity.principalName,
                AuthorityUtils.createAuthorityList("ROLE_USER"),
            )
        val refreshToken =
            OAuth2RefreshToken(
                entity.refreshTokenValue,
                entity.refreshTokenIssuedAt,
                entity.refreshTokenExpiresAt,
            )

        return OAuth2Authorization
            .withRegisteredClient(client)
            .authorizationGrantType(AuthorizationGrantType(entity.authorizationGrantType))
            .principalName(entity.principalName)
            .attributes {
                it[Principal::class.java.name] = principal
            }.refreshToken(refreshToken)
            .build()
    }
}

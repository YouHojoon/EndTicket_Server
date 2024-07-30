package ac.kr.smu.endticket.auth.infra.oauth2

import org.springframework.security.oauth2.core.OAuth2AuthenticationException
import org.springframework.security.oauth2.core.OAuth2Error
import org.springframework.security.oauth2.core.OAuth2ErrorCodes
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository

/**
 * Endticket 서비스의 RegisteredClient를 반환하는 메소드
 * @return Endticket 서비스의 RegisteredClient
 */
fun RegisteredClientRepository.findEndticketClient() =
    findByClientId("endticket")
        ?: throw OAuth2AuthenticationException(OAuth2Error(OAuth2ErrorCodes.INVALID_CLIENT))
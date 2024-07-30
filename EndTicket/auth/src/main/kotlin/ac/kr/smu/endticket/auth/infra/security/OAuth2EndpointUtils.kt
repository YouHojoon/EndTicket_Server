package ac.kr.smu.endticket.auth.infra.security

import org.springframework.security.oauth2.core.OAuth2AuthenticationException
import org.springframework.security.oauth2.core.OAuth2Error
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2RefreshTokenAuthenticationProvider

/**
 * Spring Security의 OAuth2EndpointUtils
 */
object OAuth2EndpointUtils {
    /**
     * OAuth2의 파라미터 에러
     * @param errorCode 에러 코드
     * @param parameterName 에러를 유발한 파라미터의 이름
     * @param errorUri 에러에 관련한 uri
     */
    fun parameterError(
        errorCode: String?,
        parameterName: String,
        errorUri: String? = null,
    ): OAuth2AuthenticationException {
        val error = OAuth2Error(errorCode, "OAuth 2.0 Parameter: $parameterName", errorUri)
        return OAuth2AuthenticationException(error)
    }
}

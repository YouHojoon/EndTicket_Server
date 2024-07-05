package ac.kr.smu.endticket.common.security

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.config.annotation.web.HttpSecurityDsl
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.AuthenticationEntryPoint
import org.springframework.security.web.util.matcher.IpAddressMatcher

/**
 * 로그인 관련 기본 설정
 * CSRF 비활성화 및 세션을 Stateless로 설정한다.
 */
fun HttpSecurityDsl.configLogin() {
    formLogin { disable() }
    csrf { disable() }
    sessionManagement {
        sessionCreationPolicy = SessionCreationPolicy.STATELESS
    }
}

/**
 * Swagger 관련 요청을 모두 허용하도록 설정한다.
 */
fun HttpSecurityDsl.permitAllSwaggerRequest() {
    authorizeRequests {
        authorize("/docs/**", permitAll)
        authorize("/swagger-ui/**", permitAll)
        authorize("/api-docs/**", permitAll)
    }
}

/**
 * 기본 exceptionHandling, 인증에 실패할 시 401 에러를 반환한다.
 */
fun HttpSecurityDsl.baseExceptionHandling() {
    exceptionHandling {
        authenticationEntryPoint =
            AuthenticationEntryPoint { _, response, e ->
                response.contentType = MediaType.APPLICATION_JSON_VALUE
                response.status = HttpStatus.UNAUTHORIZED.value()
                response.characterEncoding = "UTF-8"
                response.writer.write(
                    ObjectMapper().writeValueAsString(
                        mapOf(
                            "code" to HttpStatus.UNAUTHORIZED.value(),
                            "message" to "인증에 실패했습니다.",
                            "detail" to e.message,
                        ),
                    ),
                )
            }
    }
}

/**
 * 화이트 리스트만 접근을 허용하도록 설정하는 메소드
 * @param whitelist 허용할 화이트 리스트
 */
fun HttpSecurityDsl.permitOnlyWhitelistRequest(whitelist: List<String>) {
    authorizeRequests {
        whitelist.forEach {
            authorize(IpAddressMatcher(it), permitAll)
        }
        authorize(anyRequest, denyAll)
    }
}

/**
 * 기본 설정
 * form 로그인 비활성화, 세션 비활성화, 기본 exception handling 설정
 */
fun HttpSecurityDsl.baseConfig() {
    configLogin()
    permitAllSwaggerRequest()
    baseExceptionHandling()
}

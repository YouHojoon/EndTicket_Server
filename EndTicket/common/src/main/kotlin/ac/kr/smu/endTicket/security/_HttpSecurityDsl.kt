package ac.kr.smu.endTicket.security

import ac.kr.smu.endTicket.response.ExceptionResponse
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
fun HttpSecurityDsl.configLogin(){
    formLogin { disable() }
    csrf { disable() }
    sessionManagement {
        sessionCreationPolicy = SessionCreationPolicy.STATELESS
    }
}

/**
 * Swagger 관련 요청을 모두 허용하도록 설정한다.
 */
fun HttpSecurityDsl.permitAllSwaggerRequest(){
    authorizeRequests {
        authorize("/docs/**", permitAll)
        authorize("/swagger-ui/**",permitAll)
        authorize("/api-docs/**",permitAll)
    }
}

/**
 * 기본 exceptionHandling, 인증에 실패할 시 401 에러를 반환한다.
 */
fun HttpSecurityDsl.baseExceptionHandling(){
    exceptionHandling {
        authenticationEntryPoint = AuthenticationEntryPoint { _, response, e ->
            response.contentType = MediaType.APPLICATION_JSON_VALUE
            response.status = HttpStatus.UNAUTHORIZED.value()
            response.characterEncoding = "UTF-8"
            response.writer.write(
                ObjectMapper().writeValueAsString(
                ExceptionResponse(
                    code = HttpStatus.UNAUTHORIZED.value(),
                    message = "인증에 실패했습니다.",
                    detail = e.message
                )
            ))
        }
    }
}

fun HttpSecurityDsl.permitOnlyWhitelistRequest(whitelist: List<String>){
    authorizeHttpRequests {
        whitelist.forEach {
            authorize(IpAddressMatcher(it), authenticated)
        }
        authorize(anyRequest, denyAll)
    }
}

/**
 * 기본 설정
 * form 로그인 비활성화, 세션 비활성화, 기본 exception handling 설정
 */
fun HttpSecurityDsl.baseConfig(){
    configLogin()
    permitAllSwaggerRequest()
    baseExceptionHandling()
}
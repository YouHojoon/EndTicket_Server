package ac.kr.smu.endTicket.auth.infra.oauth2.filter

import ac.kr.smu.endTicket.auth.infra.oauth2.exception.OAuth2RequestException
import ac.kr.smu.endTicket.auth.infra.oauth2.idtoken.exception.JWKParseException
import ac.kr.smu.endTicket.auth.infra.oauth2.idtoken.exception.UnverifiedIdTokenException
import ac.kr.smu.endticket.common.web.response.ExceptionResponse
import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.web.filter.OncePerRequestFilter

/**
 * OAuth2 인증 과정에서 발생한 에러를 처리하는 Filter
 */
class OAuth2ErrorHandlerFilter: OncePerRequestFilter() {
    private val log = LoggerFactory.getLogger(this::class.java)
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        try {
            filterChain.doFilter(request,response)
        }catch (e: OAuth2RequestException){
            log.error("{parameters: ${
                request
                    .parameterMap
                    .map { "${it.key} : [${it.value.joinToString(", ")}]" }
                    .joinToString(", ")
            }",
                e)
            sendResponse(response, HttpStatus.INTERNAL_SERVER_ERROR)
        }catch (e: UnverifiedIdTokenException){
            log.info("{idToken: ${e.idToken}, message: ${e.message}}", e)
            sendResponse(response, HttpStatus.BAD_REQUEST)
        }catch (e: JWKParseException){
            log.error("clientName: ${e.clientName}, message: ${e.message}",e)
            sendResponse(response, HttpStatus.INTERNAL_SERVER_ERROR)
        }catch (e: IllegalArgumentException){
            log.info("{message: ${e.message}}",e)
            sendResponse(response, HttpStatus.BAD_REQUEST, message = e.message ?: "")
        }
    }

    /**
     * 응답을 보내는 메소드
     * @param response 응답을 보낼 서블렛 객체
     * @param status 응답의 statusCode
     * @param message 응답 body에 전송될 메시지
     */
    private fun sendResponse(response: HttpServletResponse, status: HttpStatus, message: String = "SNS 인증에 실패했습니다."){
        response.contentType = MediaType.APPLICATION_JSON_VALUE
        response.characterEncoding = "UTF-8"
        response.status = status.value()
        val body = ExceptionResponse(status.value(), "SNS 인증에 실패했습니다.", message)
        response.writer.write(ObjectMapper().writeValueAsString(body))
    }
}
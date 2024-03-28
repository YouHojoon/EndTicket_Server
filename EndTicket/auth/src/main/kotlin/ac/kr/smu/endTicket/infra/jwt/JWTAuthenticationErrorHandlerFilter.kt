package ac.kr.smu.endTicket.infra.jwt

import com.fasterxml.jackson.databind.ObjectMapper
import io.jsonwebtoken.ExpiredJwtException
import io.jsonwebtoken.UnsupportedJwtException
import io.jsonwebtoken.security.SignatureException
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.web.filter.OncePerRequestFilter
/**
 * JWT 인증 과정에서 발생한 에러를 처리하는 Filter
 */
class JWTAuthenticationErrorHandlerFilter: OncePerRequestFilter() {
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        try {
          filterChain.doFilter(request, response)
        }
        catch (e: ExpiredJwtException){
            sendResponse(
                response = response,
                status = HttpStatus.UNAUTHORIZED,
                message = "토큰이 만료되었습니다."
            )
        }
        catch (e: SignatureException){
            sendResponse(
                response = response,
                status = HttpStatus.BAD_REQUEST,
                message = "토큰 서명 검증에 실패하였습니다."
            )
        }
        catch (e: UnsupportedJwtException){
            sendResponse(
                response,
                status = HttpStatus.BAD_REQUEST,
                message = "잘못된 토큰입니다."
            )
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
        val body = mapOf("message" to message, "code" to status.value())
        response.writer.write(ObjectMapper().writeValueAsString(body))
    }
}
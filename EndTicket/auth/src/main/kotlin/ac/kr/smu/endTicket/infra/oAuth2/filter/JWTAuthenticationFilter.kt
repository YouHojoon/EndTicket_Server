package ac.kr.smu.endTicket.infra.oAuth2.filter

import ac.kr.smu.endTicket.auth.service.TokenService
import com.fasterxml.jackson.databind.ObjectMapper
import io.jsonwebtoken.ExpiredJwtException
import io.jsonwebtoken.UnsupportedJwtException
import io.jsonwebtoken.security.SignatureException
import jakarta.servlet.Filter
import jakarta.servlet.FilterChain
import jakarta.servlet.ServletRequest
import jakarta.servlet.ServletResponse
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import java.security.Principal

/**
 * JWT 토큰으로 인증을 하는 필터
 */
class JWTAuthenticationFilter(
    private val tokenService: TokenService
): Filter{
    private val JWT_TOKEN_HEADER_NAME = "Authorization"

    override fun doFilter(request: ServletRequest, response: ServletResponse, chain: FilterChain) {
        val httpRequest = request as HttpServletRequest
        val jwt = parseJWTToken(httpRequest)

        if (jwt == null){
            chain.doFilter(request, response)
            return
        }

        try {
            val userID = tokenService.parseUserID(jwt)

            SecurityContextHolder
                .getContext().authentication = createAuthentication(userID)

            chain.doFilter(request,response)
        }catch (e: ExpiredJwtException){
            sendResponse(
                response = response as HttpServletResponse,
                status = HttpStatus.UNAUTHORIZED,
                message = "토큰이 만료되었습니다."
            )
        }
        catch (e: SignatureException){
            sendResponse(
                response = response as HttpServletResponse,
                status = HttpStatus.BAD_REQUEST,
                message = "토큰 서명 검증에 실패하였습니다."
            )
        }
        catch (e: UnsupportedJwtException){
            sendResponse(
                response as HttpServletResponse,
                status = HttpStatus.BAD_REQUEST,
                message = "잘못된 토큰입니다."
            )
        }
    }

    /**
     * 인증 객체를 만드는 메소드
     * @param userID 사용자 ID
     */
    private fun createAuthentication(userID: Long): UsernamePasswordAuthenticationToken{
        return UsernamePasswordAuthenticationToken(
            object: Principal{
                override fun getName(): String {
                    return userID.toString()
                }
            },
            null,
            mutableListOf(SimpleGrantedAuthority("USER"))
        )
    }

    /**
     * 헤더에 존재하는 JWT를 받아오는 메소드
     * @param request 요청
     * @return JWT, 없다면 null
     */
    private fun parseJWTToken(request: HttpServletRequest): String?{
        val header = request.getHeader(JWT_TOKEN_HEADER_NAME) ?: return null
        return header.split(" ").lastOrNull()
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
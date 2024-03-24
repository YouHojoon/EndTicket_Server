package ac.kr.smu.endTicket.infra.jwt

import ac.kr.smu.endTicket.auth.service.TokenService
import com.fasterxml.jackson.databind.ObjectMapper
import io.jsonwebtoken.ExpiredJwtException
import io.jsonwebtoken.UnsupportedJwtException
import io.jsonwebtoken.security.SignatureException
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.util.matcher.AntPathRequestMatcher
import org.springframework.web.client.HttpClientErrorException.Unauthorized
import org.springframework.web.filter.OncePerRequestFilter
import java.security.Principal

/**
 * JWT 토큰으로 인증을 하는 필터
 * @property tokenService 토큰 관련 로직을 수행하는 객
 */
class JWTAuthenticationFilter(
    private val tokenService: TokenService
): OncePerRequestFilter(){
    private val JWT_TOKEN_HEADER_NAME = "Authorization"
    private val matcher = AntPathRequestMatcher("/auth/{uri:(sns|reissueToken)}")

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val jwt = parseJWTToken(request)

        if (jwt == null){
            filterChain.doFilter(request,response)
            return
        }

        val userID = tokenService.parseUserID(jwt)

        SecurityContextHolder
            .getContext().authentication = createAuthentication(userID)


        filterChain.doFilter(request,response)
    }

    /**
     * 인증 객체를 만드는 메소드
     * @param userID 사용자 ID
     * @return 인증 객체
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
}
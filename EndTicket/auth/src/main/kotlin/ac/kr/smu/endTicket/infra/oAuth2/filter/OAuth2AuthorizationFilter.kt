package ac.kr.smu.endTicket.infra.oAuth2.filter

import ac.kr.smu.endTicket.infra.oAuth2.OAuth2User
import ac.kr.smu.endTicket.auth.domain.service.OAuthService
import ac.kr.smu.endTicket.auth.ui.converter.SocialTypeConverter
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken
import org.springframework.security.web.util.matcher.AntPathRequestMatcher
import org.springframework.web.filter.OncePerRequestFilter

/**
 * 인증 요청을 처리하는 필터
 * 토큰 생성 전에 SNS의 OAuth2 서비스를 통해 인증한다.
 * @property oAuthService 인증을 처리하는 서비스 객체
 */
class OAuth2AuthorizationFilter(
    private val oAuthService: OAuthService
): OncePerRequestFilter() {
    private val converter = SocialTypeConverter()
    private val SOCIAL_TYPE_URI_VARIABLE_NAME = "socialType"
    private val CODE_URI_VARIABLE_NAME = "code"
    private val matcher = AntPathRequestMatcher("/auth/sns")

    @Throws(IllegalStateException::class)
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        if (!matcher.matches(request) || request.method != "POST"){
            filterChain.doFilter(request,response)
            return
        }

        val socialTypeString = request.getParameter(SOCIAL_TYPE_URI_VARIABLE_NAME) ?: null
        check(socialTypeString != null){"SocialType이 없습니다."}
        check(socialTypeString.isNotBlank()){"SocialType이 비어있습니다."}

        val socialType = converter.convert(socialTypeString)
        checkNotNull(socialType){"지원하지 않는 SNS입니다."}

        val code = request.getParameter(CODE_URI_VARIABLE_NAME) ?: ""
        check(code.isNotBlank()){"code가 비어있습니다."}

        val oAuth2TokenResponse = oAuthService.oAuth(socialType, code)
        val socialUserNumber = oAuthService.parseSocialUserNumber(socialType, oAuth2TokenResponse.idToken)
        val oAuth2User = OAuth2User(socialUserNumber, socialType)

        SecurityContextHolder.getContext().authentication =  OAuth2AuthenticationToken(oAuth2User, oAuth2User.authorities, socialTypeString)

        filterChain.doFilter(request,response)
    }
}
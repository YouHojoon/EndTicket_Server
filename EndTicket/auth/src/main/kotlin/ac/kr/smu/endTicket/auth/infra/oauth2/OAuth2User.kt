package ac.kr.smu.endTicket.auth.infra.oauth2

import ac.kr.smu.endTicket.auth.domain.model.SocialType
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.oauth2.core.user.OAuth2User

/**
 * OAuth2 인증을 위한 [OAuth2User] 구현체
 * @property socialUserNumber 파싱된 SNS 사용자 번호
 * @property socialType SNS 타입
 */
class OAuth2User(
    socialUserNumber: String,
    socialType: SocialType
): OAuth2User {
    private val attr = mutableMapOf<String, Any>()

    init {
        attr["socialUserNumber"] = socialUserNumber
        attr["socialType"] = socialType
    }
    override fun getName(): String {
        return attr["socialUserNumber"].toString()
    }

    override fun getAttributes(): MutableMap<String, Any> {
        return attr
    }

    override fun getAuthorities(): MutableCollection<out GrantedAuthority> {
        return mutableListOf(SimpleGrantedAuthority("USER"))
    }
}
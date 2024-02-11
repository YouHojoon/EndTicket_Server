package ac.kr.smu.endTicket.user.ui.converter

import ac.kr.smu.endTicket.user.domain.model.User
import org.springframework.core.convert.converter.Converter

class SocialTypeConverter: Converter<String, User.SocialType> {
    override fun convert(source: String): User.SocialType? {
        return User.SocialType.valueOf(source.uppercase())
    }

}
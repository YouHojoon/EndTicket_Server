package ac.kr.smu.endticket.user.domain.converter

import ac.kr.smu.endticket.user.domain.model.User
import org.springframework.core.convert.converter.Converter

class SocialTypeConverter : Converter<String, User.SocialType> {
    override fun convert(source: String) = User.SocialType.entries.firstOrNull { source.uppercase() == it.name }
}

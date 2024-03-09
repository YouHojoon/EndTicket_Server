package ac.kr.smu.endTicket.auth.ui.converter

import ac.kr.smu.endTicket.auth.domain.model.SocialType
import org.springframework.core.convert.converter.Converter

class SocialTypeConverter: Converter<String, SocialType> {
    override fun convert(source: String): SocialType? {
        return SocialType.valueOf(source.uppercase())
    }
}
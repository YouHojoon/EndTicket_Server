package ac.kr.smu.endticket.auth.domain.converter

import ac.kr.smu.endticket.auth.domain.model.SocialType
import org.springframework.core.convert.converter.Converter

class SocialTypeConverter : Converter<String, SocialType> {
    override fun convert(source: String): SocialType? {
        val name = source.uppercase()
        return SocialType.values().firstOrNull { it.name == name }
    }
}

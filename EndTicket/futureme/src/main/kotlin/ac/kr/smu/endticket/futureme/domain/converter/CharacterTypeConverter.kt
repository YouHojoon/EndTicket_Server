package ac.kr.smu.endticket.futureme.domain.converter

import ac.kr.smu.endticket.common.web.enum.CharacterType
import ac.kr.smu.endticket.futureme.domain.futureme.exception.UnsupportedCharacterException
import org.springframework.core.convert.converter.Converter

class CharacterTypeConverter : Converter<String, CharacterType> {
    override fun convert(source: String) =
        CharacterType.entries.firstOrNull { it.name == source.uppercase() } ?: throw UnsupportedCharacterException(
            source,
        )
}

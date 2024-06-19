package ac.kr.smu.endticket.futureme.domain.converter

import ac.kr.smu.endticket.futureme.domain.futureme.exception.UnsupportedCharacterException
import ac.kr.smu.endticket.futureme.domain.futureme.model.Character
import org.springframework.core.convert.converter.Converter

class CharacterTypeConverter: Converter<String, Character.Type> {
    override fun convert(source: String) = Character.Type.values().firstOrNull { it.name == source.uppercase() } ?: throw UnsupportedCharacterException(source)
}
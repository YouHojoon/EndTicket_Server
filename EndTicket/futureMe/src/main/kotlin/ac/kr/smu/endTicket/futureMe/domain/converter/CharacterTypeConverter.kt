package ac.kr.smu.endTicket.futureMe.domain.converter

import ac.kr.smu.endTicket.futureMe.domain.futureMe.model.Character
import org.springframework.core.convert.converter.Converter

class CharacterTypeConverter: Converter<String, Character.Type> {
    override fun convert(source: String) = Character.Type.values().firstOrNull { it.name == source.uppercase() }
}
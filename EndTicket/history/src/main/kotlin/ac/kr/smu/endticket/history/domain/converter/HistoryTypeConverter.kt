package ac.kr.smu.endticket.history.domain.converter

import ac.kr.smu.endticket.history.domain.model.History
import org.springframework.core.convert.converter.Converter

class HistoryTypeConverter : Converter<String, History.Type> {
    override fun convert(source: String): History.Type? =
        History.Type
            .entries
            .firstOrNull { it.name == source.uppercase() }
}

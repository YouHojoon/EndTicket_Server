package ac.kr.smu.endticket.ticket.domain.converter

import ac.kr.smu.endticket.ticket.domain.model.Ticket
import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter

@Converter(autoApply = true)
class MaxSwipeCountConverter : AttributeConverter<Ticket.MaxSwipeCount, Int> {
    override fun convertToDatabaseColumn(attribute: Ticket.MaxSwipeCount?): Int? {
        val maxSwipeCount = attribute ?: return null

        return maxSwipeCount.value
    }

    override fun convertToEntityAttribute(dbData: Int?): Ticket.MaxSwipeCount? {
        val value = dbData ?: return null
        return Ticket.MaxSwipeCount.fromValue(value)
    }
}

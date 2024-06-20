package ac.kr.smu.endticket.ticket.infra.messaging

import ac.kr.smu.endticket.common.web.enum.Color
import ac.kr.smu.endticket.common.web.enum.TicketType


data class TicketCompletedEventResponse(
    val id: Long,
    val behavior: String,
    val target: String,
    val color: Color,
    val type: TicketType,
    val swipeCount: Int
)
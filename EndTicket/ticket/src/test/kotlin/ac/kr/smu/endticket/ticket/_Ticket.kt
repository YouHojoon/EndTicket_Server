package ac.kr.smu.endticket.ticket

import ac.kr.smu.endticket.common.web.enum.Color
import ac.kr.smu.endticket.common.web.enum.TicketType
import ac.kr.smu.endticket.ticket.domain.model.Ticket
import ac.kr.smu.endticket.ticket.ui.request.TicketRequest

const val USER_ID = 1L

val TICKET_REQUEST =
    TicketRequest(
        behavior = "b",
        target = "t",
        color = Color.BLUE1,
        type = TicketType.SELF_IMPROVEMENT,
        maxSwipeCount = Ticket.MaxSwipeCount.FIVE,
)


val UPDATE_REQUEST = TicketRequest(
    "abcd",
    "abcd",
    Color.GRAY2,
    TicketType.PERSONALITY,
    Ticket.MaxSwipeCount.FIFTEEN
)
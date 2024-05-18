package ac.kr.smu.endTicket

import ac.kr.smu.endTicket.ticket.domain.model.Ticket
import ac.kr.smu.endTicket.ticket.ui.request.TicketRequest

const val USER_ID = 1L

val TICKET_REQUEST =
    TicketRequest(
        behavior = "b",
        target = "t",
        color = Ticket.Color.BLUE1,
        type = Ticket.Type.SELF_IMPROVEMENT,
        maxSwipeCount = Ticket.MaxSwipeCount.FIVE,
)


val UPDATE_REQUEST = TicketRequest(
    "abcd",
    "abcd",
    Ticket.Color.GRAY2,
    Ticket.Type.PERSONALITY,
    Ticket.MaxSwipeCount.FIFTEEN
)
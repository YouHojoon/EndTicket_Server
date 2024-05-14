package ac.kr.smu.endTicket.ticket.domain.event

import ac.kr.smu.endTicket.ticket.domain.model.Ticket
import ac.kr.smu.endTicket.ticket.ui.response.TicketResponse

data class TicketCompletionEvent private constructor(
    val response: TicketResponse,
    val userID: Long
) {
    companion object{
        fun from(response: TicketResponse, userID: Long) = TicketCompletionEvent(response, userID)
    }
}
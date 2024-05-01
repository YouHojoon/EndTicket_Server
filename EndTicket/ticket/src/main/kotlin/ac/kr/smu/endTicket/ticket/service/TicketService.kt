package ac.kr.smu.endTicket.ticket.service

import ac.kr.smu.endTicket.ticket.domain.model.Ticket
import ac.kr.smu.endTicket.ticket.domain.repository.TicketRepository
import ac.kr.smu.endTicket.ticket.ui.request.CreateTicketRequest
import org.springframework.stereotype.Service

@Service
class TicketService(
    private val repo: TicketRepository
) {
    fun createTicket(request: CreateTicketRequest, userID: Long): Ticket{
        return repo.save(
            Ticket(request,userID)
        )
    }
}
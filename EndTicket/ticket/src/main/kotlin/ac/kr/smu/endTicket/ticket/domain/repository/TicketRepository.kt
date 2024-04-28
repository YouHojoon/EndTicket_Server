package ac.kr.smu.endTicket.ticket.domain.repository

import ac.kr.smu.endTicket.ticket.domain.model.Ticket
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface TicketRepository: JpaRepository<Ticket, Long> {
}
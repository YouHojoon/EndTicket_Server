package ac.kr.smu.endTicket.ticket.domain.repository

import ac.kr.smu.endTicket.ticket.domain.model.TicketCompletionEvent
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
interface TicketCompletionEventRepository: JpaRepository<TicketCompletionEvent, Long>{
    fun findByIsSentFalseAndAuditCreatedAtBefore(date: LocalDateTime): Set<TicketCompletionEvent>
}
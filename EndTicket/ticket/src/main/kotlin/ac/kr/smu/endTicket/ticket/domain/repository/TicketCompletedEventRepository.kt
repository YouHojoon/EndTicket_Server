package ac.kr.smu.endTicket.ticket.domain.repository

import ac.kr.smu.endTicket.ticket.domain.model.TicketCompletedEvent
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
interface TicketCompletedEventRepository: JpaRepository<TicketCompletedEvent, Long>{
    fun findByIsSentFalseAndAuditCreatedAtBefore(date: LocalDateTime): Set<TicketCompletedEvent>
}
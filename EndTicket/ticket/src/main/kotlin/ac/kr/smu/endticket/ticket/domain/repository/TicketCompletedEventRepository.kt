package ac.kr.smu.endticket.ticket.domain.repository

import ac.kr.smu.endticket.ticket.domain.model.TicketCompletedEvent
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
interface TicketCompletedEventRepository : JpaRepository<TicketCompletedEvent, Long> {
    fun findByAuditCreatedAtBefore(date: LocalDateTime): Set<TicketCompletedEvent>
}

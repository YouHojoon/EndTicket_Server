package ac.kr.smu.endTicket.futureMe.domain.event.repository

import ac.kr.smu.endTicket.futureMe.domain.event.model.Event
import ac.kr.smu.endTicket.futureMe.domain.event.model.ImaginationCompletedEvent
import ac.kr.smu.endTicket.futureMe.domain.event.model.TicketCompletedEvent
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
interface EventRepository: JpaRepository<Event, Long>{
    /**
     *
     * eventID와 type을 이용해 이벤트의 존재 여부를 반환하는 메소드
     * @param eventID 이벤트 ID
     * @param type 이벤트 타입, [TicketCompletedEvent],[ImaginationCompletedEvent]
     * @return 존재 여부
     */
    fun existsByEventIDAndType(eventID: Long, type: String): Boolean
    @Query(
        "select e from ImaginationCompletedEvent e where e.isSent = false and e.audit.createdAt <= :date"
    )
    fun findNotSentEventBefore(date: LocalDateTime): Set<ImaginationCompletedEvent>
}
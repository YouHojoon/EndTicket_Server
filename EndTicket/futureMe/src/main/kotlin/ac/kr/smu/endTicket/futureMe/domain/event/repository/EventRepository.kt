package ac.kr.smu.endTicket.futureMe.domain.event.repository

import ac.kr.smu.endTicket.futureMe.domain.event.model.Event
import ac.kr.smu.endTicket.futureMe.domain.event.model.ImaginationCompletionEvent
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
interface EventRepository: JpaRepository<Event, Long>{
    /**
     *
     * eventID와 type을 이용해 이벤트를 조회하는 메소드
     * @param eventID 이벤트 ID
     * @param type 이벤트 타입, [TicketCompletionEvent],[ImaginationCompletionEvent]
     * @return 조회된 이벤트, 존재하지 않는다면 null이 반환된다.
     */
    fun findByEventIDAndType(eventID: Long, type: String): Event?

    @Query(
        "select e from ImaginationCompletionEvent e where e.isSent = false and e.audit.createdAt <= :date"
    )
    fun findNotSentEventBefore(date: LocalDateTime): Set<ImaginationCompletionEvent>
}
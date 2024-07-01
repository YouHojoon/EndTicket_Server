package ac.kr.smu.endticket.futureme.domain.event.repository

import ac.kr.smu.endticket.futureme.domain.event.model.Event
import ac.kr.smu.endticket.futureme.domain.event.model.ImaginationCompletedEvent
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
interface EventRepository: JpaRepository<Event, Long>, EventRepositorySupport{

    /**
     * 미전송된 상상해보기 완료 이벤트를 조회하는 메소드
     * @param date 조회 기준, date 이전에 발행된 이벤트들이 조회된다.
     * @return 조회된 이벤트
     */
    @Query(
        "select e from ImaginationCompletedEvent e where e.audit.createdAt <= :date"
    )
    fun findNotSentEventBefore(date: LocalDateTime): Set<ImaginationCompletedEvent>
}
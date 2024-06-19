package ac.kr.smu.endTicket.futureMe.domain.event.repository

import ac.kr.smu.endTicket.futureMe.domain.event.model.Event
import ac.kr.smu.endTicket.futureMe.domain.event.model.ImaginationCompletedEvent
import ac.kr.smu.endTicket.futureMe.domain.event.model.TicketCompletedEvent
import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import org.springframework.stereotype.Repository
import kotlin.reflect.KClass


@Repository
class EventRepositorySupportImpl: EventRepositorySupport {
    @PersistenceContext
    private lateinit var em: EntityManager

    override fun existsBySpecificIDAndType(specificID: Long, type: KClass<out Event>): Boolean{
        val (table, spec) = when(type){
            TicketCompletedEvent::class -> "ticket_completed_event" to "ticket_id"
            ImaginationCompletedEvent::class -> "imagination_completed_event" to "imagination_id"
            else -> throw IllegalArgumentException("$type 은 지원하지 않는 이벤트 타입입니다.")
        }


        val query = "SELECT EXISTS (SELECT id FROM $table WHERE $spec = :specificID)"
        return (em.createNativeQuery(query).setParameter("specificID", specificID).singleResult as Number).toInt() == 1
    }
}
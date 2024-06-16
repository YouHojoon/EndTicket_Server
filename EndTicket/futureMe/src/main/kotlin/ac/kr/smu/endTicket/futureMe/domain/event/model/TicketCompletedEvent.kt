package ac.kr.smu.endTicket.futureMe.domain.event.model

import jakarta.persistence.Entity


/**
 * 티켓 완료 이벤트
 * @property id 완료된 ticket의 id
 * @property userID ticket의 소유자 id
 */
@Entity
class TicketCompletedEvent(
    id: Long,
    userID: Long
): Event(id, userID)
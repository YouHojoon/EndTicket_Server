package ac.kr.smu.endTicket.futureMe.domain.event

import jakarta.persistence.Entity


/**
 * 티켓 완료 이벤트
 * @param id 완료된 ticket의 id
 * @param userID ticket의 소유자 id
 */
@Entity
class TicketCompletionEvent(
    id: Long,
    userID: Long
): Event(id, userID)
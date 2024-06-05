package ac.kr.smu.endTicket.futureMe.domain.event

import ac.kr.smu.endTicket.futureMe.domain.event.Event

/**
 * 상상해보기 완료 이벤트
 */
class FutureMeCompletionEvent(eventID: Long, userID: Long) : Event(eventID, userID) {
}
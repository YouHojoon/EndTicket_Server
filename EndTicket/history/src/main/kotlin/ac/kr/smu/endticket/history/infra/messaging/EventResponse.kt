package ac.kr.smu.endticket.history.infra.messaging

/**
 * 이벤트 응답
 * @param id 이벤트의 id
 */
sealed class EventResponse(val id: Long)
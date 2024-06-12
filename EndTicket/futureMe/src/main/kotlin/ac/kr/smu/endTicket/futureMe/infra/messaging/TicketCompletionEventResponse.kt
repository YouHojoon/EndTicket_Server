package ac.kr.smu.endTicket.futureMe.infra.messaging

/**
 * 티켓을 반환해줄 때 사용하는 객체
 * @property id 티켓의 ID
 */
data class TicketCompletionEventResponse(
    val id: Long = 0L,
)
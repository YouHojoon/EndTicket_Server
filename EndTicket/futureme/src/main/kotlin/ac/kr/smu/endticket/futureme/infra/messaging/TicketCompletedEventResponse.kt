package ac.kr.smu.endticket.futureme.infra.messaging

/**
 * 티켓 완료 이벤트 응답 클래스
 * @property id 티켓의 Id
 */
data class TicketCompletedEventResponse(
    val id: Long = 0L,
)

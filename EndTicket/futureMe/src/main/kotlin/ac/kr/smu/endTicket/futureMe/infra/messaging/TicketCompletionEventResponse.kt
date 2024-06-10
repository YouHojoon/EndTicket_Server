package ac.kr.smu.endTicket.futureMe.infra.messaging

import io.swagger.v3.oas.annotations.media.Schema

/**
 * 티켓을 반환해줄 때 사용하는 객체
 * @property id 티켓의 ID
 */
@Schema(description = "티켓에 대한 응답")
data class TicketCompletionEventResponse(
    val id: Long = 0L,
)
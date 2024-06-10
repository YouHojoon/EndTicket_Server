package ac.kr.smu.endTicket.ticket.infra.messaging

import ac.kr.smu.endTicket.ticket.ui.response.TicketResponse

/**
 * kafka 메시지 전송을 위한 객체
 * @property key kafka 메시지의 key
 * @property payload kafka 메시지의 payload
 */
data class TicketCompletionEventMessage(
    val key: Long,
    val payload: TicketResponse
)
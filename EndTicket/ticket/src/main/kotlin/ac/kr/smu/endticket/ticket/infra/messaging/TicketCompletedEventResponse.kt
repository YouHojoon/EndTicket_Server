package ac.kr.smu.endticket.ticket.infra.messaging

import ac.kr.smu.endticket.common.web.enum.Color
import ac.kr.smu.endticket.common.web.enum.TicketType
import java.time.LocalDateTime

/**
 * 티켓 완료 메시지의 응답
 * @property id 티켓의 id
 * @property behavior 행동
 * @property target 목표
 * @property color 색
 * @property type 티켓의 종류
 * @property swipeCount 스와이프 횟수
 * @property completedAt 완료 일자
 */
data class TicketCompletedEventResponse(
    val id: Long,
    val behavior: String,
    val target: String,
    val color: Color,
    val type: TicketType,
    val swipeCount: Int,
    val completedAt: LocalDateTime,
)

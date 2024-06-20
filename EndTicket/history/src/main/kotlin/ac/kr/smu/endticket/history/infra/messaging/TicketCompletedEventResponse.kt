package ac.kr.smu.endticket.history.infra.messaging

import ac.kr.smu.endticket.history.domain.model.Color
import ac.kr.smu.endticket.history.domain.model.TicketHistory

/**
 * 티켓 완료 이벤트의 응답
 * @property id 티켓 id
 * @property behavior 행동
 * @property target 목표
 * @property color 색
 * @property type 분류
 * @property swipeCount 스와이프 횟수
 */
class TicketCompletedEventResponse(
    id: Long,
    val behavior: String,
    val target: String,
    val color: Color,
    val type: TicketHistory.Type,
    val swipeCount: Int,
): EventResponse(id)
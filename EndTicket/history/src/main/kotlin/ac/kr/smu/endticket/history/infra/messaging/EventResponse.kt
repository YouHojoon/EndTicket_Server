package ac.kr.smu.endticket.history.infra.messaging

import java.time.LocalDateTime

/**
 * 이벤트 응답
 * @property id 이벤트의 id
 * @property completedAt 이벤트의 완료 일자
 */
sealed class EventResponse(
    val id: Long,
    val completedAt: LocalDateTime
)
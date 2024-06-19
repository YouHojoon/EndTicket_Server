package ac.kr.smu.endTicket.futureme.infra.messaging

import ac.kr.smu.endTicket.futureme.domain.imagination.model.Imagination
import java.time.LocalDateTime

/**
 * 상상해보기 완료 이벤트 응답을 위한 클래스
 * @property id 상상해보기의 id
 * @property behavior 행동
 * @property target 목표
 * @property color 색
 * @property completedDate 완료 날짜
 */
data class ImaginationCompletedEventResponse(
    val id: Long,
    val behavior: String,
    val target: String,
    val color: Imagination.Color,
    val completedDate: LocalDateTime
)
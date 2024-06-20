package ac.kr.smu.endticket.history.infra.messaging

import ac.kr.smu.endticket.history.domain.model.Color

/**
 * 상상해보기 완료 이벤트 응답
 * @property id 상상해보기 id
 * @property behavior 행동
 * @property target 목표
 * @property color 색
 */
class ImaginationCompletedEventResponse(
    id: Long,
    val behavior: String,
    val target: String,
    val color: Color,
): EventResponse(id)
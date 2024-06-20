package ac.kr.smu.endticket.history.infra.messaging

import ac.kr.smu.endticket.common.web.enum.CharacterType
import ac.kr.smu.endticket.common.web.enum.Color
import java.time.LocalDateTime

/**
 * 상상해보기 완료 이벤트 응답
 * @property id 상상해보기 id
 * @property behavior 행동
 * @property target 목표
 * @property color 색
 * @property characterType 캐릭터 종류
 * @property completedAt 이벤트의 완료 일자
 */
class ImaginationCompletedEventResponse(
    id: Long,
    val behavior: String,
    val target: String,
    val color: Color,
    val characterType: CharacterType,
    completedAt: LocalDateTime
): EventResponse(id, completedAt)
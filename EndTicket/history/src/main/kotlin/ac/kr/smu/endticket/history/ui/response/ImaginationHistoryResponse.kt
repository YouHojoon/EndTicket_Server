package ac.kr.smu.endticket.history.ui.response

import ac.kr.smu.endticket.common.web.enum.CharacterType
import ac.kr.smu.endticket.common.web.enum.Color
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDateTime

/**
 * 상상해보기 기록 응답
 * @param behavior 행동
 * @param target 목표
 * @param color 색
 * @param characterType 완료 당시 캐릭터
 * @param completedAt 완료 시간
 */
@Schema(description = "상상해보기 기록 응답")
class ImaginationHistoryResponse(
    @Schema(description = "행동", example = "운동하기")
    val behavior: String,
    @Schema(description = "목표", example = "체력도 늘고 할력도 되찾는 나의 모습")
    val target: String,
    @Schema(description = "색", example = "BLUE1")
    val color: Color,
    @Schema(description = "캐릭터", example = "CHEESE")
    val characterType: CharacterType,
    @Schema(description = "완료 일자", example = "2024-06-22T12:53:58.834278")
    completedAt: LocalDateTime,
) : HistoryResponse(completedAt)

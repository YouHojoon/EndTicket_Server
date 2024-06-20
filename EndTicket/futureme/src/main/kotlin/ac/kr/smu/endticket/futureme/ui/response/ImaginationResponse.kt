package ac.kr.smu.endticket.futureme.ui.response

import ac.kr.smu.endticket.common.web.enum.Color
import ac.kr.smu.endticket.futureme.domain.imagination.model.Imagination
import io.swagger.v3.oas.annotations.media.Schema

/**
 * 상상해보기 응답 클래스
 * @property behavior 행동
 * @property target 목표
 * @property color 색
 */
@Schema(description = "상상해보기에 대한 응답 클래스")
data class ImaginationResponse(
    @Schema(description = "상상해보기의 id", example = "1")
    val id: Long,
    @Schema(description = "행동", example = "운동하기")
    val behavior: String,
    @Schema(description = "목표", example = "체력도 늘고 할력도 되찾는 나의 모습")
    val target: String,
    @Schema(description = "색", example = "BLUE1")
    val color: Color,
)
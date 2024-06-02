package ac.kr.smu.endTicket.futureMe.ui.request

import ac.kr.smu.endTicket.futureMe.domain.imagination.model.Imagination
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

/**
 * 상상해보기 생성/수정을 위한 객체
 * @property behavior 행동
 * @property target 목표
 * @property color 색
 */
@Schema(description = "상상해보기 생성/수정을 요청")
data class ImaginationRequest(
    @Schema(description = "행동", example = "운동하기")
    @field:Size(max = 10)
    @field:NotBlank
    val behavior: String,

    @Schema(description = "목표", example = "체력도 늘고 할력도 되찾는 나의 모습")
    @field:Size(max = 20)
    @field:NotBlank
    val target: String,

    @Schema(description = "색")
    val color: Imagination.Color
)
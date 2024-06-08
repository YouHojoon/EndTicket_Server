package ac.kr.smu.endTicket.futureMe.ui.request

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.Size

/**
 * 미래의 나의 제목을 변경하는 요청
 * @property title 변경할 제목
 */
@Schema(description = "미래의 나의 제목을 변경하는 요청")
data class UpdateFutureMeTitleRequest(
    @Schema(description = "변경할 제목", example = "당당하고 멋있는 사람")
    @field:Size(max = 13)
    val title: String
)
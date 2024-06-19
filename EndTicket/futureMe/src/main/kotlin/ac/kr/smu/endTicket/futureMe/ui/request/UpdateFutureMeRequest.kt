package ac.kr.smu.endTicket.futureMe.ui.request

import ac.kr.smu.endTicket.futureMe.domain.futureMe.model.Character
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.Size

/**
 * 미래의 나의 제목을 변경하는 요청
 * @property title 변경할 제목
 * @property type 변경할 캐릭터 종류
 */
@Schema(description = "미래의 나를 변경하는 요청")
data class UpdateFutureMeRequest(
    @Schema(description = "변경할 제목", example = "당당하고 멋있는 사람")
    @field:Size(max = 13, message = "제목은 13자 이내이어야 합니다.")
    val title: String? = null,

    @Schema(description = "변경할 캐릭터 종류", implementation = Character.Type::class)
    val type: Character.Type? = null
){
    fun isEmpty() = title == null && type == null
}
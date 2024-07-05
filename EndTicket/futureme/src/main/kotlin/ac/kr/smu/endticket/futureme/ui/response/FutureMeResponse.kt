package ac.kr.smu.endticket.futureme.ui.response

import ac.kr.smu.endticket.futureme.domain.futureme.model.Character
import io.swagger.v3.oas.annotations.media.Schema

/**
 * 미래의 나 응답
 * @property title 제목
 * @property character 캐릭터
 */
@Schema(description = "미래의 나 응답")
data class FutureMeResponse(
    @Schema(description = "미래의 나 제목", example = "당당하고 멋있는 사람")
    val title: String,
    val character: Character,
)

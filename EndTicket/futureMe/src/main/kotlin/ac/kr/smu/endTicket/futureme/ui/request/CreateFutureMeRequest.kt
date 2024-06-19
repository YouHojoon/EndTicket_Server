package ac.kr.smu.endTicket.futureme.ui.request

import ac.kr.smu.endTicket.futureme.domain.futureme.model.Character
import io.swagger.v3.oas.annotations.media.Schema

/**
 * 미래의 나 생성 요청
 * @property type 생성할 미래의 나의 캐릭터
 */
@Schema(description = "미래의 나 생성 요청")
data class CreateFutureMeRequest(
    @Schema(description = "생성할 미래의 나의 캐릭터", implementation = Character.Type::class)
    val type: Character.Type
)
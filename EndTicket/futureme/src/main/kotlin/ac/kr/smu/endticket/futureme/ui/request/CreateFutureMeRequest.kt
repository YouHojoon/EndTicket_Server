package ac.kr.smu.endticket.futureme.ui.request

import ac.kr.smu.endticket.common.web.enum.CharacterType
import io.swagger.v3.oas.annotations.media.Schema

/**
 * 미래의 나 생성 요청
 * @property type 생성할 미래의 나의 캐릭터
 */
@Schema(description = "미래의 나 생성 요청")
data class CreateFutureMeRequest(
    @Schema(description = "생성할 미래의 나의 캐릭터", implementation = CharacterType::class)
    val type: CharacterType,
)

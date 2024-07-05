package ac.kr.smu.endticket.common.web.enum

import io.swagger.v3.oas.annotations.media.Schema
import org.springframework.core.io.ClassPathResource

/**
 * 캐릭터의 종류
 * @property KIA 키아
 * @property CHEESE 치즈
 * @property VEGA 베가
 * @property imageResource 캐릭터 이미지 리소스
 */
@Schema(description = "캐릭터의 종류")
enum class CharacterType {
    @Schema(description = "키아")
    KIA,

    @Schema(description = "치즈")
    CHEESE,

    @Schema(description = "베가")
    VEGA,

    ;

    var imageResource = ClassPathResource("characters/${name.lowercase()}.svg")
}

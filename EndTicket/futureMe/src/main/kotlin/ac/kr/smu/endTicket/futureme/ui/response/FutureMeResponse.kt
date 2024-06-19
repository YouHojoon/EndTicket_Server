package ac.kr.smu.endTicket.futureme.ui.response

import ac.kr.smu.endTicket.futureme.domain.futureme.model.Character
import ac.kr.smu.endTicket.futureme.domain.futureme.model.FutureMe
import io.swagger.v3.oas.annotations.media.Schema

/**
 * 미래의 나 응답
 * @property title 제목
 * @property character 캐릭터
 */
@Schema(description = "미래의 나 응답")
data class FutureMeResponse private constructor(
    @Schema(description = "미래의 나 제목", example = "당당하고 멋있는 사람")
    val title: String,
    val character: Character
){
    companion object{
        /**
         * 미래의 나로부터 응답을 생성하는 메소드
         * @param 미래의 나
         * @return 생성된 응답
         */
        fun from(futureMe: FutureMe) = FutureMeResponse(
            title = futureMe.title,
            character = futureMe.character
        )
    }
}

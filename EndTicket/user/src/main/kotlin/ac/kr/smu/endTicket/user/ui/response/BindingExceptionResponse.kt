package ac.kr.smu.endTicket.user.ui.response

import ErrorResponse
import io.swagger.v3.oas.annotations.media.Schema

/**
 *  검증을 실패해 반환되는 응답, [ErrorResponse]를 상속
 *  @param field 검증에 실패한 대표 필드
 *
 */
@Schema(description = "검증을 실패해 반환되는 응답")
class BindingExceptionResponse(
    @Schema(description = "검증에 실패한 대표 필드", example = "nickname")
    val field: String?,
    @Schema(description = "에러 코드, 현재는 HttpStatusCode", example = "400")
    code: Int,
    @Schema(description = "검증에 실패한 객체", example = "registerNicknameRequest")
    message: String?,
    @Schema(description = "검증에 실패한 사유", example = "닉네임의 길이는 3자에서 8자 이하여야 합니다.")
    detail: String?
): ErrorResponse(code,message, detail){
    override fun toString(): String {
        return "field: $field ${super.toString()}"
    }
}
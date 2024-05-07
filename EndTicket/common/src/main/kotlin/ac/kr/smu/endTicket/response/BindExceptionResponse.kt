package ac.kr.smu.endTicket.response

import io.swagger.v3.oas.annotations.media.Schema

/**
 *  검증을 실패해 반환되는 응답, [ExceptionResponse]를 상속
 *  @param field 검증에 실패한 대표 필드
 *
 */
@Schema(description = "검증을 실패해 반환되는 응답")
class BindExceptionResponse(
    @Schema(description = "검증에 실패한 대표 필드", example = "nickname")
    val field: String?,
    @Schema(description = "에러 코드, 현재는 HttpStatusCode", example = "400")
    code: Int,
    objectName: String,
    @Schema(description = "검증에 실패한 사유", example = "닉네임의 길이는 3자에서 8자 이하여야 합니다.")
    detail: String?
): ExceptionResponse(code,"$objectName 바인딩 중에 오류가 발생했습니다.", detail){
    override fun toString(): String {
        return "field: $field ${super.toString()}"
    }
}
package ac.kr.smu.endTicket.user.ui.response

import io.swagger.v3.oas.annotations.media.Schema

/**
 *  검증을 실패해 반환되는 응답
 *  @param field 검증에 실패한 대표 필드
 *  @param message 검증에 실패한 사유
 */
@Schema(description = "검증을 실패해 반환되는 응답")
data class BindingExceptionResponse(
    @Schema(description = "검증에 실패한 대표 필드", example = "nickname")
    val field: String?,
    @Schema(description = "검증에 실패한 사유", example = "크기가 3에서 8 사이여야 합니다")
    val message: String?
)
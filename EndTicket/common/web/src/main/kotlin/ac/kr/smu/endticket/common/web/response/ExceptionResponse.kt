package ac.kr.smu.endticket.common.web.response

import io.swagger.v3.oas.annotations.media.Schema

/**
 * 에러 발생 시 반환을 정의하는 클래스
 * @property code 에러 번호, 현재는 HttpStatusCode
 * @property message 간단한 에러 메시지
 * @property detail 에러 발생의 자세한 이유
 */
@Schema(description = "에러 발생 시 반환을 정의하는 클래스")
open class ExceptionResponse(
    @Schema(description = "에러 코드, 현재는 HttpStatusCode", example = "404")
    val code: Int,
    @Schema(description = "간단한 에러 메시지", example = "인증 과정 중 에러가 발생했습니다.")
    val message: String? = null,
    @Schema(description = "에러 발생의 자세한 이유", example = "access 토큰이 존재하지 않습니다.")
    val detail: String? = null,
) {
    override fun toString(): String = "{code: $code, message: $message, detail: $detail}"
}

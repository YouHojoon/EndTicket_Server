package ac.kr.smu.endTicket.auth.swagger.apiresponses

import ac.kr.smu.endTicket.auth.ui.response.TokenResponse
import ac.kr.smu.endticket.common.web.response.ExceptionResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses

@Operation(
    summary = "refresh 토큰을 사용해 토큰 재발급",
    description = "refresh 토큰을 사용해 access 토큰을 재발급 받는다.<br>만약 refresh 토큰도 일정 기준 시간 아래라면 재발급받는다.",
)
@ApiResponses(
    value = [
        ApiResponse(
            description = "재발급 성공",
            responseCode = "200",
            content = [
                Content(
                    schema = Schema(implementation = TokenResponse::class),
                ),
            ],
        ),
        ApiResponse(
            description = "잘못된 refresh 토큰",
            responseCode = "400",
            content = [Content(schema = Schema(implementation = ExceptionResponse::class))],
        ),
    ],
)
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class ReissueTokenApiResponses

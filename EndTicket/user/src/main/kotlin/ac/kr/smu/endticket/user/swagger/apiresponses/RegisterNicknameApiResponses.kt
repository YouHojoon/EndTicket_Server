package ac.kr.smu.endticket.user.swagger.apiresponses

import ac.kr.smu.endticket.common.web.response.BindExceptionResponse
import ac.kr.smu.endticket.common.web.response.ExceptionResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses

@Operation(
    summary = "닉네임을 등록하는 메소드",
    description = "닉네임이 등록되지 않은 사용자의 닉네임을 등록합니다.",
)
@ApiResponses(
    value = [
        ApiResponse(
            responseCode = "204",
            description = "닉네임 등록 완료",
        ),
        ApiResponse(
            responseCode = "404",
            description = "사용자가 존재하지 않을 때",
            content = [
                Content(schema = Schema(implementation = ExceptionResponse::class)),
            ],
        ),
        ApiResponse(
            responseCode = "400",
            description = "요청 파라미터 에러",
            content = [
                Content(schema = Schema(implementation = BindExceptionResponse::class)),
            ],
        ),
    ],
)
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class RegisterNicknameApiResponses

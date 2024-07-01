package ac.kr.smu.endticket.user.swagger.apiresponses

import ac.kr.smu.endticket.common.web.response.ExceptionResponse
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses

@ApiResponses(
    ApiResponse(
        responseCode = "204",
        description = "회원 탈퇴 완료"
    ),
    ApiResponse(
        responseCode = "404",
        description = "사용자가 존재하지 않을 때",
        content = [
            Content(schema = Schema(implementation = ExceptionResponse::class))
        ]
    )
)
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class DeleteUserApiResponses
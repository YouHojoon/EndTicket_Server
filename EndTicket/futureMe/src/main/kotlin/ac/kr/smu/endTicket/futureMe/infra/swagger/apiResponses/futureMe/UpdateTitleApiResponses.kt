package ac.kr.smu.endTicket.futureMe.infra.swagger.apiResponses.futureMe

import ac.kr.smu.endTicket.common.web.response.ExceptionResponse
import ac.kr.smu.endTicket.futureMe.domain.futureMe.model.FutureMe
import ac.kr.smu.endTicket.futureMe.ui.response.FutureMeResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses

@Operation(description = "미래의 나 제목 등록/변경")
@ApiResponses(
    value = [
        ApiResponse(
            responseCode = "200",
            description = "미래의 나 제목 등록/변경 성공",
            content = [
                Content(schema = Schema(implementation = FutureMeResponse::class))
            ]
        ),
        ApiResponse(
            responseCode = "404",
            description = "미래의 나가 존재하지 않음",
            content = [
                Content(schema = Schema(implementation = ExceptionResponse::class))
            ]
        )
    ]
)
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class UpdateTitleApiResponses
package ac.kr.smu.endticket.futureme.infra.swagger.apiResponses.futureMe

import ac.kr.smu.endticket.common.web.response.ExceptionResponse
import ac.kr.smu.endticket.futureme.ui.response.FutureMeResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses

@Operation(description = "미래의 나 생성")
@ApiResponses(
    value = [
        ApiResponse(
            responseCode = "201",
            description = "생성 성공",
            content = [
                Content(schema = Schema(implementation = FutureMeResponse::class))
            ]
        ),
        ApiResponse(
            responseCode = "409",
            description = "미래의 나가 이미 존재",
            content = [
                Content(schema = Schema(implementation = ExceptionResponse::class))
            ]
        )
    ]
)
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class CreateFutureMeApiResponses
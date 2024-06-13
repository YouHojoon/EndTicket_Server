package ac.kr.smu.endTicket.futureMe.infra.swagger.apiResponses.futureMe

import ac.kr.smu.endTicket.futureMe.domain.futureMe.model.FutureMe
import ac.kr.smu.endTicket.response.ExceptionResponse
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
                Content(schema = Schema(implementation = FutureMe::class))
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
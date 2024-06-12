package ac.kr.smu.endTicket.futureMe.infra.swagger.apiResponses.futureMe

import ac.kr.smu.endTicket.futureMe.domain.futureMe.model.FutureMe
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
        )
    ]
)
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class CreateFutureMeApiResponses
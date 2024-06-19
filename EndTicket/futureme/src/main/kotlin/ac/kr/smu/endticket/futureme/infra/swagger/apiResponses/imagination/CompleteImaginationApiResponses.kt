package ac.kr.smu.endticket.futureme.infra.swagger.apiResponses.imagination

import ac.kr.smu.endticket.common.web.response.ExceptionResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses

@ApiResponses(
    ApiResponse(
        responseCode = "204",
        description = "티켓 완료"
    ),
    ApiResponse(
        responseCode = "403",
        description = "소유자가 아닌 사용자",
        content = [Content(schema = Schema(implementation = ExceptionResponse::class))]
    ),
    ApiResponse(
        responseCode = "404",
        description = "존재하지 않는 상상해보기",
        content = [Content(schema = Schema(implementation = ExceptionResponse::class))]
    )
)
@Operation(description = "티켓 완료")
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class CompleteImaginationApiResponses

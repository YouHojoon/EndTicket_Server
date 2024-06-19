package ac.kr.smu.endTicket.ticket.infra.swagger

import ac.kr.smu.endticket.common.web.response.ExceptionResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses

@Operation(description = "티켓 삭제")
@ApiResponses(
    ApiResponse(
        responseCode = "204",
        description = "삭제 성공"
    ),
    ApiResponse(
        responseCode = "403",
        description = "소유자가 아닌 사용자 티켓 삭제 요청",
        content = [Content(schema = Schema(implementation = ExceptionResponse::class))]
    ),
    ApiResponse(
        responseCode = "404",
        description = "존재하지 않는 티켓",
        content = [Content(schema = Schema(implementation = ExceptionResponse::class))]
    )
)
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class DeleteTicketResponses()

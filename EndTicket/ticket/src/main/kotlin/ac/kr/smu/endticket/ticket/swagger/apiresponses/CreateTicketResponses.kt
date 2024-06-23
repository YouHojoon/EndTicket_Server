package ac.kr.smu.endticket.ticket.swagger.apiresponses

import ac.kr.smu.endticket.common.web.response.BindExceptionResponse
import ac.kr.smu.endticket.ticket.ui.response.TicketResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION)
@ApiResponses(
    ApiResponse(
        description = "생성 성공",
        responseCode = "201",
        content = [Content(schema = Schema(implementation = TicketResponse::class))]
    ),
    ApiResponse(
        description = "비정상적인 생성 요청",
        responseCode = "400",
        content = [Content(schema = Schema(implementation = BindExceptionResponse::class))]
    )
)
@Operation(summary = "티켓 생성")
annotation class CreateTicketResponses

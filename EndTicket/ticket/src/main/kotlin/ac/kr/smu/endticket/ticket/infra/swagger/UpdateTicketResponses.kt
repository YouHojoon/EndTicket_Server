package ac.kr.smu.endticket.ticket.infra.swagger

import ac.kr.smu.endticket.common.web.response.BindExceptionResponse
import ac.kr.smu.endticket.common.web.response.ExceptionResponse
import ac.kr.smu.endticket.ticket.ui.response.TicketResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses

@ApiResponses(
    ApiResponse(
        description = "수정 성공",
        responseCode = "200",
        content = [Content(schema = Schema(implementation = TicketResponse::class))]
    ),

    ApiResponse(
        description = "비정상적인 수정 요청",
        responseCode = "400",
        content = [Content(schema = Schema(implementation = BindExceptionResponse::class))]
    ),

    ApiResponse(
        description = "티켓 소유자가 아닌 사용자의 티켓 수정 요청",
        responseCode = "403",
        content = [Content(schema = Schema(implementation = ExceptionResponse::class))]
    ),

    ApiResponse(
        description = "존재하지 않는 티켓의 수정 요청",
        responseCode = "404",
        content = [Content(schema = Schema(implementation = ExceptionResponse::class))]
    )
)
@Operation(summary = "티켓 수정")
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class UpdateTicketResponses

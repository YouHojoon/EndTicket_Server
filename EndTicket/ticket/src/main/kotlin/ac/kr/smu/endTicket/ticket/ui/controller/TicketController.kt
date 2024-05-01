package ac.kr.smu.endTicket.ticket.ui.controller

import response.BindExceptionResponse
import ac.kr.smu.endTicket.ticket.domain.model.Ticket
import ac.kr.smu.endTicket.ticket.service.TicketService
import ac.kr.smu.endTicket.ticket.ui.request.CreateTicketRequest
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/tickets")
@Tag(name = "/tickets")
class TicketController(
    private val service: TicketService
) {
    @ApiResponses(
        ApiResponse(
            description = "생성 완료",
            responseCode = "201",
            content = [Content(schema = Schema(implementation = Ticket::class))]
        ),
        ApiResponse(
            description = "파라미터 에러",
            responseCode = "400",
            content = [Content(schema = Schema(implementation = BindExceptionResponse::class))]
        )
    )
    @Operation(summary = "티켓 생성", security = [SecurityRequirement(name = "Access token")])
    @PostMapping
    fun createTicket(
        @Parameter(name = "생성 요청", schema = Schema(implementation = CreateTicketRequest::class), required = true)
        @RequestBody
        @Valid
        ticket: CreateTicketRequest,

        @RequestHeader("X-User-ID")
        @Parameter(hidden = true)
        userID: Long
    ): ResponseEntity<Ticket>{
        return ResponseEntity.status(HttpStatus.CREATED).body(service.createTicket(ticket, userID))
    }

}
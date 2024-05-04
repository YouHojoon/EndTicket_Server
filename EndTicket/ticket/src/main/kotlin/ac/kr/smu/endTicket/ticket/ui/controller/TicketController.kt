package ac.kr.smu.endTicket.ticket.ui.controller

import ac.kr.smu.endTicket.ticket.domain.exception.NotOwnerOfTicketException
import ac.kr.smu.endTicket.response.BindExceptionResponse
import ac.kr.smu.endTicket.ticket.domain.model.Ticket
import ac.kr.smu.endTicket.ticket.service.TicketService
import ac.kr.smu.endTicket.ticket.ui.request.TicketRequest
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
import org.springframework.web.ErrorResponse
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import ac.kr.smu.endTicket.response.ExceptionResponse
import org.slf4j.LoggerFactory

@RestController
@RequestMapping("/tickets")
@Tag(name = "/tickets")
class TicketController(
    private val service: TicketService
) {
    private val log = LoggerFactory.getLogger(TicketController::class.java)
    @ApiResponses(
        ApiResponse(
            description = "생성 완료",
            responseCode = "201",
            content = [Content(schema = Schema(implementation = Ticket::class))]
        ),
        ApiResponse(
            description = "비정상적인 생성 요청",
            responseCode = "400",
            content = [Content(schema = Schema(implementation = BindExceptionResponse::class))]
        )
    )
    @Operation(summary = "티켓 생성", security = [SecurityRequirement(name = "Access token")])
    @PostMapping
    fun createTicket(
        @Valid
        @RequestBody
        @Parameter(name = "생성 요청", schema = Schema(implementation = TicketRequest::class), required = true)
        request: TicketRequest,

        @RequestHeader("X-User-ID")
        @Parameter(hidden = true)
        userID: Long
    ): ResponseEntity<Ticket>{
        return ResponseEntity.status(HttpStatus.CREATED).body(service.createTicket(request, userID))
    }

    @ApiResponses(
        ApiResponse(
            description = "수정 완료",
            responseCode = "200",
            content = [Content(schema = Schema(implementation = Ticket::class))]
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
    @PutMapping("/{id}")
    fun updateTicket(
        @Valid
        @RequestBody
        @Parameter(description = "수정 요청", schema = Schema(implementation = TicketRequest::class), required = true)
        request: TicketRequest,

        @Parameter(description = "티켓의 id", example = "1", required = true)
        @PathVariable id: Long,

        @RequestHeader("X-User-ID")
        @Parameter(hidden = true)
        userID: Long
    ): ResponseEntity<*>{
        val message = "티켓 수정 중 오류가 발생했습니다."
        try {
            return ResponseEntity
                .ok(service.updateTicket(request, id, userID))
        }catch (e: IllegalStateException){
            log.info("id: $id", e)
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                ExceptionResponse(
                code = HttpStatus.NOT_FOUND.value(),
                message = message,
                detail = e.message
            )
            )
        }
        catch (e: NotOwnerOfTicketException){
            log.info("id: $id, userID: $userID", e)
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                ExceptionResponse(
                    code = HttpStatus.FORBIDDEN.value(),
                    message = message,
                    detail = "사용자가 티켓의 소유자가 아닙니다."
                )
            )
        }
    }
}
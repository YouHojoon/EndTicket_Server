package ac.kr.smu.endticket.ticket.ui.controller

import ac.kr.smu.endticket.common.constant.HttpHeaderName
import ac.kr.smu.endticket.common.web.response.ExceptionResponse
import ac.kr.smu.endticket.ticket.domain.exception.TicketNotFoundException
import ac.kr.smu.endticket.ticket.domain.exception.TicketOwnershipException
import ac.kr.smu.endticket.ticket.service.TicketService
import ac.kr.smu.endticket.ticket.swagger.apiresponses.*
import ac.kr.smu.endticket.ticket.ui.request.TicketRequest
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/tickets")
@Tag(name = "/tickets")
@SecurityRequirement(name = "Access token")
class TicketController(
    private val service: TicketService,
) {
    private val log = LoggerFactory.getLogger(TicketController::class.java)

    @CreateTicketResponses
    @PostMapping
    fun createTicket(
        @Valid
        @RequestBody
        @Parameter(name = "생성 요청", schema = Schema(implementation = TicketRequest::class), required = true)
        request: TicketRequest,
        @RequestHeader(HttpHeaderName.USER_ID)
        @Parameter(hidden = true)
        userId: Long,
    ): ResponseEntity<*> =
        try {
            ResponseEntity.status(HttpStatus.CREATED).body(service.createTicket(request, userId))
        } catch (e: IllegalStateException) {
            log.info("티켓 생성 실패 : {userId: $userId}", e)
            ResponseEntity.status(HttpStatus.CONFLICT).body(
                ExceptionResponse(
                    code = HttpStatus.CONFLICT.value(),
                    message = "티켓 생성 중 오류가 발생했습니다.",
                    detail = e.message,
                ),
            )
        }

    @UpdateTicketResponses
    @PutMapping("/{id}")
    fun updateTicket(
        @Valid
        @RequestBody
        @Parameter(description = "수정 요청", schema = Schema(implementation = TicketRequest::class), required = true)
        request: TicketRequest,
        @Parameter(description = "티켓의 Id", example = "1", required = true)
        @PathVariable id: Long,
        @RequestHeader(HttpHeaderName.USER_ID)
        @Parameter(hidden = true)
        userId: Long,
    ): ResponseEntity<*> = ResponseEntity.ok(service.updateTicket(request, id, userId))

    @SwipeTicketResponses
    @PatchMapping("/swipe/{id}")
    fun swipeTicket(
        @PathVariable("id")
        @Parameter(description = "티켓의 Id", example = "1", required = true)
        id: Long,
        @Parameter(hidden = true)
        @RequestHeader(HttpHeaderName.USER_ID)
        userId: Long,
    ): ResponseEntity<*> = ResponseEntity.ok(service.swipeTicket(id, userId))

    @CancelSwipeTicketResponses
    @DeleteMapping("/swipe/{id}")
    fun cancelSwipeTicket(
        @Parameter(description = "티켓의 Id", example = "1", required = true)
        @PathVariable("id")
        id: Long,
        @Parameter(hidden = true)
        @RequestHeader(HttpHeaderName.USER_ID)
        userId: Long,
    ): ResponseEntity<*> = ResponseEntity.ok(service.cancelSwipeTicket(id, userId))

    @FindIncompleteTicketResponses
    @GetMapping
    fun findIncompleteTicket(
        @Parameter(hidden = true)
        @RequestHeader(HttpHeaderName.USER_ID)
        userId: Long,
    ): ResponseEntity<*> = ResponseEntity.ok(mapOf("tickets" to service.findIncompleteTickets(userId)))

    @DeleteTicketResponses
    @DeleteMapping("{id}")
    fun deleteTicket(
        @Parameter(description = "티켓의 Id", example = "1", required = true)
        @PathVariable("id")
        id: Long,
        @Parameter(hidden = true)
        @RequestHeader(HttpHeaderName.USER_ID)
        userId: Long,
    ): ResponseEntity<Unit> {
        service.deleteTicket(id, userId)
        return ResponseEntity.noContent().build()
    }

    @ExceptionHandler(TicketNotFoundException::class)
    fun handleTicketNotFoundException(e: TicketNotFoundException): ResponseEntity<ExceptionResponse> {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
            ExceptionResponse(
                code = HttpStatus.NOT_FOUND.value(),
                message = "티켓 조회에 에러가 발생했습니다.",
                detail = e.message,
            ),
        )
    }

    @ExceptionHandler(TicketOwnershipException::class)
    fun handleTicketOwnershipException(e: TicketOwnershipException): ResponseEntity<ExceptionResponse> {
        log.info("티켓 소유권 에러 : {id: ${e.id}, userId: ${e.userId}}", e)
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
            ExceptionResponse(
                code = HttpStatus.FORBIDDEN.value(),
                message = "티켓 스와이프 혹은 수정 요청 시 에러가 발생했습니다.",
                detail = "사용자가 티켓의 소유자가 아닙니다.",
            ),
        )
    }
}

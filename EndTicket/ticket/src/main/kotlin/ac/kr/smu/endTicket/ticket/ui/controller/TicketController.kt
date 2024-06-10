package ac.kr.smu.endTicket.ticket.ui.controller

import ac.kr.smu.endTicket.response.ExceptionResponse
import ac.kr.smu.endTicket.ticket.domain.exception.NotFoundTicketException
import ac.kr.smu.endTicket.ticket.domain.exception.NotOwnerOfTicketException
import ac.kr.smu.endTicket.ticket.infra.swagger.*
import ac.kr.smu.endTicket.ticket.service.TicketService
import ac.kr.smu.endTicket.ticket.ui.request.TicketRequest
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
    private val service: TicketService
) {
    private val log = LoggerFactory.getLogger(TicketController::class.java)
    @CreateTicketResponse
    @PostMapping
    fun createTicket(
        @Valid
        @RequestBody
        @Parameter(name = "생성 요청", schema = Schema(implementation = TicketRequest::class), required = true)
        request: TicketRequest,

        @RequestHeader("X-User-ID")
        @Parameter(hidden = true)
        userID: Long
    ): ResponseEntity<*>{
        return try{
            ResponseEntity.status(HttpStatus.CREATED).body(service.createTicket(request, userID))
        }
        catch (e: IllegalStateException){
            log.info("userID: $userID", e)
            ResponseEntity.status(HttpStatus.CONFLICT).body(
                ExceptionResponse(
                    code = HttpStatus.CONFLICT.value(),
                    message = "티켓 생성 중 오류가 발생했습니다.",
                    detail = e.message
                )
            )
        }

    }

    @UpdateTicketResponse
    @PutMapping("/{id}")
    fun updateTicket(
        @Valid
        @RequestBody
        @Parameter(description = "수정 요청", schema = Schema(implementation = TicketRequest::class), required = true)
        request: TicketRequest,

        @Parameter(description = "티켓의 ID", example = "1", required = true)
        @PathVariable id: Long,

        @RequestHeader("X-User-ID")
        @Parameter(hidden = true)
        userID: Long
    ): ResponseEntity<*> = ResponseEntity.ok(service.updateTicket(request, id, userID))

    @SwipeTicketResponse
    @PatchMapping("/swipe/{id}")
    fun swipeTicket(
        @PathVariable("id")
        @Parameter(description = "티켓의 ID", example = "1",required = true)
        id: Long,

        @Parameter(hidden = true)
        @RequestHeader("X-User-ID")
        userID: Long
    ): ResponseEntity<*> = ResponseEntity.ok(service.swipeTicket(id,userID))


    @CancelSwipeTicketResponse
    @DeleteMapping("/swipe/{id}")
    fun cancelSwipeTicket(
        @Parameter(description = "티켓의 ID", example = "1", required = true)
        @PathVariable("id")
        id: Long,

        @Parameter(hidden = true)
        @RequestHeader("X-User-ID")
        userID: Long
    ): ResponseEntity<*> = ResponseEntity.ok(service.cancelSwipeTicket(id, userID))

    @FindIncompleteTicketResponse
    @GetMapping
    fun findIncompleteTicket(
        @Parameter(hidden = true)
        @RequestHeader("X-User-ID")
        userID: Long
    ): ResponseEntity<*> = ResponseEntity.ok(mapOf("tickets" to service.findIncompleteTicket(userID)))

    @DeleteTicketResponse
    @DeleteMapping("{id}")
    fun deleteTicket(
        @Parameter(description = "티켓의 ID", example = "1", required = true)
        @PathVariable("id")
        id: Long,

        @Parameter(hidden = true)
        @RequestHeader("X-User-ID")
        userID: Long
    ): ResponseEntity<Void>{
        service.deleteTicket(id,userID)
        return ResponseEntity.noContent().build()
    }
    @ExceptionHandler(NotFoundTicketException::class)
    fun handleNotFoundTicketException(e: NotFoundTicketException): ResponseEntity<ExceptionResponse>{
        log.info("id: ${e.id}", e)
        return  ResponseEntity.status(HttpStatus.NOT_FOUND).body(
            ExceptionResponse(
                code = HttpStatus.NOT_FOUND.value(),
                message = "티켓 조회에 에러가 발생했습니다.",
                detail = e.message
            )
        )
    }
    @ExceptionHandler(NotOwnerOfTicketException::class)
    fun handleNotOwnerOfTicketException(e: NotOwnerOfTicketException): ResponseEntity<ExceptionResponse>{
        log.info("id: ${e.id}, userID: ${e.userID}", e)
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
            ExceptionResponse(
                code = HttpStatus.FORBIDDEN.value(),
                message = "티켓 스와이프 혹은 수정 요청 시 에러가 발생했습니다.",
                detail = "사용자가 티켓의 소유자가 아닙니다."
            )
        )
    }
}
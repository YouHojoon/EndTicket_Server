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
import ac.kr.smu.endTicket.ticket.domain.exception.NotFoundTicketException
import ac.kr.smu.endTicket.ticket.ui.response.TicketResponse
import com.fasterxml.jackson.databind.ObjectMapper
import io.swagger.v3.oas.annotations.media.ArraySchema
import io.swagger.v3.oas.annotations.media.SchemaProperty
import io.swagger.v3.oas.annotations.security.SecurityRequirements
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping

@RestController
@RequestMapping("/tickets")
@Tag(name = "/tickets")
@SecurityRequirement(name = "Access token")
class TicketController(
    private val service: TicketService,
    private val om: ObjectMapper
) {
    private val log = LoggerFactory.getLogger(TicketController::class.java)
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
    @PostMapping
    fun createTicket(
        @Valid
        @RequestBody
        @Parameter(name = "생성 요청", schema = Schema(implementation = TicketRequest::class), required = true)
        request: TicketRequest,

        @RequestHeader("X-User-ID")
        @Parameter(hidden = true)
        userID: Long
    ): ResponseEntity<TicketResponse> = ResponseEntity.status(HttpStatus.CREATED).body(service.createTicket(request, userID))

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

    @ApiResponses(
        ApiResponse(
            description = "스와이프 성공",
            responseCode = "200",
            content = [
                Content(schema = Schema(implementation = TicketResponse::class))
            ]
        ),
        ApiResponse(
            description = "티켓 소유자가 아닌 사용자의 티켓 스와이프 요청",
            responseCode = "403",
            content = [Content(schema = Schema(implementation = ExceptionResponse::class))]
        ),

        ApiResponse(
            description = "존재하지 않는 티켓의 스와이프 요청",
            responseCode = "404",
            content = [Content(schema = Schema(implementation = ExceptionResponse::class))]
        )
    )
    @Operation(summary = "티켓 스와이프", description = "티켓을 스와이프 합니다. 티켓이 완료일 시에는 완료 이벤트가 발생됩니다.")
    @PatchMapping("/swipe/{id}")
    fun swipeTicket(
        @PathVariable("id")
        @Parameter(description = "티켓의 ID", example = "1",required = true)
        id: Long,

        @Parameter(hidden = true)
        @RequestHeader("X-User-ID")
        userID: Long
    ): ResponseEntity<*> = ResponseEntity.ok(service.swipeTicket(id,userID))

    @ApiResponses(
        ApiResponse(
            description = "스와이프 취소 성공",
            responseCode = "200",
            content = [
                Content(schema = Schema(implementation = TicketResponse::class))
            ]
        ),
        ApiResponse(
            description = "티켓 소유자가 아닌 사용자의 티켓 스와이프 취소 요청",
            responseCode = "403",
            content = [Content(schema = Schema(implementation = ExceptionResponse::class))]
        ),

        ApiResponse(
            description = "존재하지 않는 티켓의 스와이프 취소 요청",
            responseCode = "404",
            content = [Content(schema = Schema(implementation = ExceptionResponse::class))]
        )
    )
    @DeleteMapping("/swipe/{id}")
    @Operation(summary = "티켓 스와이프 취소")
    fun cancelSwipeTicket(
        @Parameter(description = "티켓의 ID", example = "1", required = true)
        @PathVariable("id")
        id: Long,

        @Parameter(hidden = true)
        @RequestHeader("X-User-ID")
        userID: Long
    ): ResponseEntity<*> = ResponseEntity.ok(service.cancelSwipeTicket(id, userID))

    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "조회 성공",
                content = [
                    Content(
                        schema = Schema(type = "object", requiredProperties = ["tickets"]),
                        schemaProperties = [
                            SchemaProperty(name = "tickets", array = ArraySchema(schema = Schema(implementation = TicketResponse::class)))
                        ]
                    )
                ])
        ]
    )
    @Operation(summary = "미완료된 티켓 조회")
    @GetMapping
    fun findIncompleteTicket(
        @Parameter(hidden = true)
        @RequestHeader("X-User-ID")
        userID: Long
    ): ResponseEntity<*> = ResponseEntity.ok(mapOf("tickets" to service.findIncompleteTicket(userID)))

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
package ac.kr.smu.endTicket.futureMe.ui.controller

import ac.kr.smu.endTicket.constant.HttpHeaderName
import ac.kr.smu.endTicket.futureMe.domain.imagination.exception.NotFoundImaginationException
import ac.kr.smu.endTicket.futureMe.domain.imagination.exception.NotOwnerOfImaginationException
import ac.kr.smu.endTicket.futureMe.domain.imagination.model.Imagination
import ac.kr.smu.endTicket.futureMe.service.ImaginationService
import ac.kr.smu.endTicket.futureMe.ui.request.ImaginationRequest
import ac.kr.smu.endTicket.futureMe.ui.response.ImaginationResponse
import ac.kr.smu.endTicket.response.BindExceptionResponse
import ac.kr.smu.endTicket.response.ExceptionResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.ArraySchema
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.media.SchemaProperty
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/imaginations")
@Tag(name = "/imaginations")
@SecurityRequirement(name = "Access token")
class ImaginationController(
    private val service: ImaginationService
) {
    private val log = LoggerFactory.getLogger(ImaginationController::class.java)

    @GetMapping
    @Operation(description = "상상해보기 나 조회")
    @ApiResponses(
        ApiResponse(
            responseCode = "200",
            description = "조회 성공",
            content = [
                Content(
                    schema =
                    Schema(
                        type = "object",
                        requiredProperties = ["imaginations"]
                    ),
                    schemaProperties = [
                        SchemaProperty(name = "imaginations", array = ArraySchema(items = Schema(implementation = ImaginationResponse::class)))
                    ]
                )
            ]
        )
    )
    fun findImaginations(
        @RequestHeader(HttpHeaderName.USER_ID)
        @Parameter(hidden = true)
        userID: Long
    ) = ResponseEntity.ok(
        mapOf("imaginations" to service.findImaginations(userID))
    )

    @PostMapping
    @Operation(description = "상상해보기 생성")
    @ApiResponses(
        ApiResponse(
            responseCode = "201",
            description = "생성 성공",
            content = [
                Content(schema = Schema(implementation = ImaginationResponse::class))
            ]
        ),
        ApiResponse(
            responseCode = "400",
            description = "비정상적인 요청",
            content = [Content(schema = Schema(implementation = BindExceptionResponse::class))]
        ),
        ApiResponse(
            responseCode = "409",
            description = "최대 개수 이상으로 생성 요청",
            content = [
                Content(schema = Schema(implementation = ExceptionResponse::class))
            ]
        )
    )
    fun createImagination(
        @RequestBody
        @Parameter(
            description = "생성 요청",
            schema = Schema(implementation = ImaginationRequest::class),
            required = true
        )
        @Valid
        request: ImaginationRequest,

        @RequestHeader(HttpHeaderName.USER_ID)
        @Parameter(hidden = true)
        userID: Long
    ) =
        try {
            ResponseEntity
                .status(HttpStatus.CREATED)
                .body(service.createImagination(request,userID))
        }catch (e: IllegalStateException){
            log.info("{userID: $userID}",e)
            val status = HttpStatus.CONFLICT
            ResponseEntity
                .status(status)
                .body(
                    ExceptionResponse(
                        code = status.value(),
                        message = "미래의 나 생성에서 에러가 발생했습니다.",
                        detail = e.message
                    )
                )
        }

    @PutMapping("{id}")
    @Operation(description = "상상해보기 수정")
    @ApiResponses(
        ApiResponse(
            responseCode = "200",
            description = "수정 성공",
            content = [Content(schema = Schema(implementation = ImaginationResponse::class))]
        ),
        ApiResponse(
            responseCode = "400",
            description = "비정상적인 요청",
            content = [Content(schema = Schema(implementation = BindExceptionResponse::class))]
        ),
        ApiResponse(
            responseCode = "404",
            description = "존재하지 않는 상상해보기",
            content = [Content(schema = Schema(implementation = ExceptionResponse::class))]
        ),
        ApiResponse(
            responseCode = "403",
            description = "소유자가 아닌 사용자",
            content = [Content(schema = Schema(implementation = ExceptionResponse::class))]
        )
    )

    fun updateImagination(
        @PathVariable("id")
        @Parameter(
            description = "상상해보기 id",
            required = true,
            example = "1"
        )
        id: Long,

        @RequestBody
        @Parameter(
            description = "수정 요청",
            required = true,
            schema = Schema(implementation = ImaginationRequest::class)
        )
        @Valid
        request: ImaginationRequest,

        @RequestHeader(HttpHeaderName.USER_ID)
        @Parameter(hidden = true)
        userID: Long
    ) = ResponseEntity.ok(service.updateImagination(request, id, userID))

    @DeleteMapping("{id}")
    @Operation(description = "상상해보기 삭제")
    @ApiResponses(
        ApiResponse(
            responseCode = "204",
            description = "삭제 성공"
        ),
        ApiResponse(
            responseCode = "404",
            description = "존재하지 않는 상상해보기",
            content = [Content(schema = Schema(implementation = ExceptionResponse::class))]
        ),
        ApiResponse(
            responseCode = "403",
            description = "소유자가 아닌 사용자",
            content = [Content(schema = Schema(implementation = ExceptionResponse::class))]
        )
    )
    fun deleteImagination(
        @PathVariable("id")
        @Parameter(
            description = "상상해보기 id",
            example = "1",
            required = true
        )
        id: Long,

        @RequestHeader(HttpHeaderName.USER_ID)
        @Parameter(hidden = true)
        userID: Long
    ): ResponseEntity<Void>{
        service.deleteImagination(id,userID)
        return ResponseEntity.noContent().build()
    }


    @ExceptionHandler(NotFoundImaginationException::class)
    fun handleNotFoundImaginationException(e: NotFoundImaginationException): ResponseEntity<ExceptionResponse>{
        log.info("${e.id}",e)
        val status = HttpStatus.NOT_FOUND

        return ResponseEntity.status(status).body(
            ExceptionResponse(
                code = status.value(),
                message = "상상해보기 조회에 에러가 발생했습니다.",
                detail = e.message
            )
        )
    }

    @ExceptionHandler(NotOwnerOfImaginationException::class)
    fun handleNotOwnerOfImaginationException(e: NotOwnerOfImaginationException): ResponseEntity<ExceptionResponse>{
        log.info("{id: ${e.id}, userID: ${e.userID}}",e)
        val status = HttpStatus.FORBIDDEN

        return ResponseEntity.status(status).body(
            ExceptionResponse(
                code = status.value(),
                message = "상상해보기 요청 중 에러가 발생했습니다.",
                detail = e.message
            )
        )
    }
}
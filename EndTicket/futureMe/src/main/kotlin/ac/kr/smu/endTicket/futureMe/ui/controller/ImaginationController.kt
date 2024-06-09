package ac.kr.smu.endTicket.futureMe.ui.controller

import ac.kr.smu.endTicket.constant.HttpHeaderName
import ac.kr.smu.endTicket.futureMe.domain.imagination.model.Imagination
import ac.kr.smu.endTicket.futureMe.service.ImaginationService
import ac.kr.smu.endTicket.futureMe.ui.request.ImaginationRequest
import ac.kr.smu.endTicket.futureMe.ui.response.ImaginationResponse
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
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
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
}
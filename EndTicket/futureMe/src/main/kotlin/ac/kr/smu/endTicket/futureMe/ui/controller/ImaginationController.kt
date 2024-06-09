package ac.kr.smu.endTicket.futureMe.ui.controller

import ac.kr.smu.endTicket.constant.HttpHeaderName
import ac.kr.smu.endTicket.futureMe.domain.imagination.model.Imagination
import ac.kr.smu.endTicket.futureMe.service.ImaginationService
import ac.kr.smu.endTicket.futureMe.ui.response.ImaginationResponse
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
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
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
    @Operation(description = "미래의 나 조회")
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
    fun findImagination(
        @RequestHeader(HttpHeaderName.USER_ID)
        @Parameter(hidden = true)
        userID: Long
    ) = ResponseEntity.ok(
        mapOf("imaginations" to service.findImaginations(userID))
    )
}
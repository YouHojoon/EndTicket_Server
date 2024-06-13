package ac.kr.smu.endTicket.futureMe.infra.swagger.apiResponses.imagination

import ac.kr.smu.endTicket.futureMe.ui.response.ImaginationResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.ArraySchema
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.media.SchemaProperty
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses

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
                    SchemaProperty(name = "imaginations", array = ArraySchema(schema = Schema(implementation = ImaginationResponse::class), maxItems = 6))
                ]
            )
        ]
    )
)
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class FindImaginationsApiResponses

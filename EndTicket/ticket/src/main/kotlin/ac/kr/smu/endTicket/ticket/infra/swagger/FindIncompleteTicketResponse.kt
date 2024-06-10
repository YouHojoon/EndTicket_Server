package ac.kr.smu.endTicket.ticket.infra.swagger

import ac.kr.smu.endTicket.ticket.ui.response.TicketResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.ArraySchema
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.media.SchemaProperty
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses

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
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class FindIncompleteTicketResponse

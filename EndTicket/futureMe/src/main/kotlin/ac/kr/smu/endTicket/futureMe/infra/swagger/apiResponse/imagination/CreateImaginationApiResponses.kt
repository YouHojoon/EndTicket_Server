package ac.kr.smu.endTicket.futureMe.infra.swagger.apiResponse.imagination

import ac.kr.smu.endTicket.futureMe.ui.response.ImaginationResponse
import ac.kr.smu.endTicket.response.BindExceptionResponse
import ac.kr.smu.endTicket.response.ExceptionResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses

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
annotation class CreateImaginationApiResponses

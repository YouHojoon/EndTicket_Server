package ac.kr.smu.endticket.futureme.swagger.apiresponses.imagination

import ac.kr.smu.endticket.common.web.response.BindExceptionResponse
import ac.kr.smu.endticket.common.web.response.ExceptionResponse
import ac.kr.smu.endticket.futureme.ui.response.ImaginationResponse
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

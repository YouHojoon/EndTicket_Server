package ac.kr.smu.endticket.futureme.swagger.apiresponses.imagination

import ac.kr.smu.endticket.common.web.response.BindExceptionResponse
import ac.kr.smu.endticket.common.web.response.ExceptionResponse
import ac.kr.smu.endticket.futureme.ui.response.ImaginationResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses

@Operation(description = "상상해보기 수정", summary = "상상해보기 수정")
@ApiResponses(
    ApiResponse(
        responseCode = "200",
        description = "수정 성공",
        content = [Content(schema = Schema(implementation = ImaginationResponse::class))],
    ),
    ApiResponse(
        responseCode = "400",
        description = "비정상적인 요청",
        content = [Content(schema = Schema(implementation = BindExceptionResponse::class))],
    ),
    ApiResponse(
        responseCode = "403",
        description = "소유자가 아닌 사용자",
        content = [Content(schema = Schema(implementation = ExceptionResponse::class))],
    ),
    ApiResponse(
        responseCode = "404",
        description = "존재하지 않는 상상해보기",
        content = [Content(schema = Schema(implementation = ExceptionResponse::class))],
    ),
)
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class UpdateImaginationApiResponses

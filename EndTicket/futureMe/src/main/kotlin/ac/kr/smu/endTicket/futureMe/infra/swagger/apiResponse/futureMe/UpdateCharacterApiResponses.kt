package ac.kr.smu.endTicket.futureMe.infra.swagger.apiResponse.futureMe

import ac.kr.smu.endTicket.futureMe.domain.futureMe.model.FutureMe
import ac.kr.smu.endTicket.response.ExceptionResponse
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses

@ApiResponses(
    ApiResponse(
        responseCode = "200",
        description = "캐릭터 변경 성공",
        content = [Content(schema = Schema(implementation = FutureMe::class))]
    ),
    ApiResponse(
        responseCode = "404",
        description = "미래의 나가 존재하지 않음",
        content = [Content(schema = Schema(implementation = ExceptionResponse::class))]
    )
)
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class UpdateCharacterApiResponses

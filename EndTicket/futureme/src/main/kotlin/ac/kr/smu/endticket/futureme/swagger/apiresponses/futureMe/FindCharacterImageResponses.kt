package ac.kr.smu.endticket.futureme.swagger.apiresponses.futureMe

import ac.kr.smu.endticket.common.web.response.ExceptionResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses

@Operation(description = "캐릭터 이미지 조회", summary = "캐릭터 이미지 조회")
@ApiResponses(
    value = [
        ApiResponse(
            responseCode = "200",
            description = "조회 성공",
            content = [
                Content(
                    mediaType = "image/svg+xml",
                    schema = Schema(type = "string", format = "binary"),
                ),
            ],
        ),
        ApiResponse(
            responseCode = "404",
            description = "존재하지 않는 캐릭터",
            content = [
                Content(schema = Schema(implementation = ExceptionResponse::class)),
            ],
        ),
    ],
)
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class FindCharacterImageResponses

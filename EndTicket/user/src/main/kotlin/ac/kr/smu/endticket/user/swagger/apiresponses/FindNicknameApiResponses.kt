package ac.kr.smu.endticket.user.swagger.apiresponses

import ac.kr.smu.endticket.common.web.response.ExceptionResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.media.SchemaProperty
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses

@ApiResponses(
    ApiResponse(
        responseCode = "200",
        description = "조회 성공",
        content = [Content(
            schema = Schema(type = "object", requiredProperties = ["nickname"]),
            schemaProperties = [
                SchemaProperty(name = "nickname", schema = Schema(type = "string", example = "닉네임"))
            ]
        )]
    ),
    ApiResponse(
        responseCode = "404",
        description = "사용자가 존재하지 않을 때",
        content = [
            Content(schema = Schema(implementation = ExceptionResponse::class))
        ]
    ),
)
@Operation(description = "사용자 닉네임 조회", summary = "사용자 닉네임 조회")
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class FindNicknameApiResponses {
}
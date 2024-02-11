package ac.kr.smu.endTicket.auth.ui.controller

import ac.kr.smu.endTicket.auth.domain.exception.UserNotFoundException
import ac.kr.smu.endTicket.auth.service.AuthService
import ac.kr.smu.endTicket.auth.domain.model.SocialType
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.media.SchemaProperty
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import org.springframework.http.HttpStatus

import org.springframework.http.HttpStatusCode

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController


@RequestMapping("/auth")
@RestController
class AuthController(
    private val authService: AuthService
) {
    @PostMapping("{SNS}/{code}")
    @Operation(summary = "SNS를 사용해 인증", description = "SNS 로그인으로 발급받은 authorization code를 이용해 인증<br>회원 정보가 없을 시 status code 404와 함께 SNS 회원 번호를 응답")
    @ApiResponses(
        value = [
            ApiResponse(description = "인증 성공", responseCode = "200",
                content = [
                    Content(
                        schema = Schema(type = "object", requiredProperties = ["token"]),
                        schemaProperties = [
                            SchemaProperty(name = "token", schema = Schema(type = "string", example = "12345"))
                        ]
                    )
                ]),
            ApiResponse(description = "파라미터 에러", responseCode = "400"),
            ApiResponse(description = "회원 정보 없음", responseCode = "404",
                content = [
                    Content(
                        schema = Schema(type = "object", requiredProperties = ["socialUserNumber"]),
                        schemaProperties = [
                            SchemaProperty(name = "socialUserNumber", schema = Schema(type = "string", example = "12345"))
                        ]
                    )])
        ]
    )
    fun auth(
        @Parameter(description = "인증에 사용한 SNS", required = true)
        @PathVariable("SNS")
             SNS: SocialType,

        @Parameter(description = "SNS 로그인으로 발급받은 authorization code", required = true)
        @PathVariable("code")
        code: String
    ): ResponseEntity<*>{
        try {
            val jwtToken = authService.auth(SNS,code)
            return ResponseEntity.ok(jwtToken)
        }catch (e:UserNotFoundException){
            return ResponseEntity(mapOf("socialUserNumber" to e.socialUserNumber), HttpStatus.NOT_FOUND)
        }
    }
}
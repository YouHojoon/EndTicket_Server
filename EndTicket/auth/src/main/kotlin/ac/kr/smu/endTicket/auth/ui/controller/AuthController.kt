package ac.kr.smu.endTicket.auth.ui.controller

import ac.kr.smu.endTicket.auth.domain.exception.UserNotFoundException
import ac.kr.smu.endTicket.auth.domain.model.JWTToken
import ac.kr.smu.endTicket.infra.oAuth2.OAuth2User
import ac.kr.smu.endTicket.auth.service.AuthService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.media.SchemaProperty
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import org.springframework.http.HttpStatus

import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController


@RequestMapping("/auth")
@RestController
class AuthController(
    private val authService: AuthService
) {
    @PostMapping("/{socialType}")
    @Operation(summary = "SNS를 사용해 토큰 생성", description = "SNS 로그인으로 발급받은 authorization code를 이용해 Access 토큰과 Refresh 토큰 생성<br>회원 정보가 없을 시 status code 404와 함께 SNS 회원 번호를 응답")
    @ApiResponses(
        value = [
            ApiResponse(description = "인증 성공", responseCode = "200",
                content = [
                    Content(
                        schema = Schema(implementation = JWTToken::class)
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
    fun createToken(
        @RequestParam("code") code: String,
        @AuthenticationPrincipal oAuth2User: OAuth2User
    ): ResponseEntity<*>{
        try {
            val jwtToken = authService.createToken(oAuth2User.socialType, oAuth2User.name)
            return ResponseEntity.ok(jwtToken)
        }catch (e:UserNotFoundException){
            return ResponseEntity(mapOf("socialUserNumber" to e.socialUserNumber), HttpStatus.NOT_FOUND)
        }
    }
}
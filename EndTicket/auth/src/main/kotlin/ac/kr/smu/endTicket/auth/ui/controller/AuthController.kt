package ac.kr.smu.endTicket.auth.ui.controller

import ac.kr.smu.endTicket.auth.domain.model.SocialType
import ac.kr.smu.endTicket.auth.domain.service.UserService
import ac.kr.smu.endTicket.auth.infra.oauth2.OAuth2User
import ac.kr.smu.endTicket.auth.service.TokenService
import ac.kr.smu.endTicket.auth.ui.response.TokenResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.StringToClassMapItem
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.media.SchemaProperty
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus

import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import ac.kr.smu.endTicket.response.ExceptionResponse
import io.swagger.v3.oas.annotations.tags.Tag


@RequestMapping("/auth")
@RestController
@Tag(name = "/auth")
class AuthController(
    private val tokenService: TokenService,
    private val userService: UserService
) {
    private val log = LoggerFactory.getLogger(AuthController::class.java)

    @PostMapping("/sns")
    @Operation(summary = "SNS를 사용해 토큰 생성", description = "SNS 로그인으로 발급받은 authorization code를 이용해 Access 토큰과 Refresh 토큰 생성<br>회원 정보가 없을 시 가입 요청한다.")
    @ApiResponses(
        value = [
            ApiResponse(description = "인증 성공", responseCode = "200",
                content = [
                    Content(
                        schema = Schema(implementation = TokenResponse::class)
                    )
                ]),
            ApiResponse(description = "파라미터 에러", responseCode = "400")
        ]
    )
    fun createToken(
        @Parameter(description = "인증에 사용한 SNS", schema = Schema(implementation = SocialType::class))
        @RequestParam("socialType")
        socialType: SocialType,

        @Parameter(description = "인증 후 받은 authorization code", example = "d-MKlV0eZz8d6x9upP3Z7mTd8w2nRSHMHORMV01xfAnMhtFN0n6MyGP-LyMKPXOaAAABjiie8OaBPKUF0hG4dQ")
        @RequestParam("code")
        code: String,

        @AuthenticationPrincipal
        oAuth2User: OAuth2User
    ): ResponseEntity<*>{
        val userID = userService.findUserID(socialType, oAuth2User.name)

        if (userID == -1L)
            return ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(ExceptionResponse(503, "토큰을 발급하는 과정에서 에러가 발생했습니다.", "user서버와 통신에 실패했습니다"))


        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(tokenService.createAccessAndRefreshToken(userID))

    }

    @Operation(summary = "refresh 토큰을 사용해 토큰 재발급", description = "refresh 토큰을 사용해 access 토큰을 재발급 받는다.<br>만약 refresh 토큰도 일정 기준 시간 아래라면 재발급받는다.")
    @ApiResponses(
        value = [
            ApiResponse(
                description = "재발급 성공",
                responseCode = "200",
                content = [
                    Content(
                        schema = Schema(implementation = TokenResponse::class)
                    )
                ]),
            ApiResponse(
                description = "파라미터 에러",
                responseCode = "400",
                content = [
                    Content(
                        schema = Schema(type = "object", requiredProperties = ["message", "code"]),
                        schemaProperties = [
                            SchemaProperty(name = "message", schema = Schema(type = "string", example = "refresh 토큰이 없습니다.")),
                            SchemaProperty(name = "code", schema = Schema(type = "integer", example = "400"))
                        ]
                    )
                ])
        ]
    )
    @PostMapping("/reissue-token")
    fun reissueToken(
        @Parameter(
            description = "refresh 토큰",
            schema = Schema(
                type = "object",
                requiredProperties = ["refreshToken"],
                properties = [StringToClassMapItem(String::class, key = "refreshToken")]
            ),
        )
        @RequestBody body: Map<String, String>
    ): ResponseEntity<*>{
        val message = "토큰을 재발급하는 과정에서 에러가 발생했습니다."
        val refreshToken = body["refreshToken"] ?: return ResponseEntity.badRequest().body(ExceptionResponse(400,message,"refresh 토큰이 존재하지 않습니다."))

        try {
            val token = tokenService.reissueToken(refreshToken)

            return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(token)
        }catch (e: IllegalStateException){
            log.info("{refreshToken: $refreshToken, message: ${e.message}}", e)

            return ResponseEntity
                .badRequest()
                .body(ExceptionResponse(400, message, e.message))
        }
    }
}
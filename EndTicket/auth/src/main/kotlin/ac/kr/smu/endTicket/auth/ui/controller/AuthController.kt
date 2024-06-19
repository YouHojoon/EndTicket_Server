package ac.kr.smu.endTicket.auth.ui.controller

import ac.kr.smu.endTicket.auth.domain.model.SocialType
import ac.kr.smu.endTicket.auth.service.UserService
import ac.kr.smu.endTicket.auth.infra.oauth2.OAuth2User
import ac.kr.smu.endTicket.auth.infra.swagger.apiResponses.CreateTokenResponses
import ac.kr.smu.endTicket.auth.infra.swagger.apiResponses.ReissueTokenApiResponses
import ac.kr.smu.endTicket.auth.service.TokenService
import ac.kr.smu.endticket.common.web.response.ExceptionResponse
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.StringToClassMapItem
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.tags.Tag
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*


@RequestMapping("/auth")
@RestController
@Tag(name = "/auth")
class AuthController(
    private val tokenService: TokenService,
    private val userService: UserService
) {
    private val log = LoggerFactory.getLogger(AuthController::class.java)

    @CreateTokenResponses
    @PostMapping("/sns")
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
                .body(ExceptionResponse(503, "토큰을 발급하는 과정에서 에러가 발생했습니다.", "시용자 서버와 통신에 실패했습니다"))

        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(tokenService.createAccessAndRefreshToken(userID))
    }

    @ReissueTokenApiResponses
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
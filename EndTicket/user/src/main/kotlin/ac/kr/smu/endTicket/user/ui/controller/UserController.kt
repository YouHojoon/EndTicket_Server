package ac.kr.smu.endTicket.user.ui.controller

import ac.kr.smu.endTicket.user.domain.service.UserService
import ac.kr.smu.endTicket.user.ui.request.RegisterNicknameRequest

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import ac.kr.smu.endTicket.response.BindExceptionResponse
import ac.kr.smu.endTicket.response.ExceptionResponse

@RestController
@RequestMapping("/users")
@Tag(name = "/users")
class  UserController(
    private val service: UserService
) {
    private val log = LoggerFactory.getLogger(UserController::class.java)
    @PostMapping("nickname")
    @Operation(summary = "닉네임을 등록하는 메소드", description = "닉네임이 등록되지 않은 사용자의 닉네임을 등록합니다.", security = [SecurityRequirement(name = "Access token")])
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "204",
                description = "닉네임 등록 완료"
            ),
            ApiResponse(
                responseCode = "404",
                description = "사용자를 찾을 수 없음",
                content = [
                    Content(schema = Schema(implementation = ExceptionResponse::class))
                ]
            ),
            ApiResponse(
                responseCode = "400",
                description = "요청 파라미터 에러",
                content = [
                    Content(schema = Schema(implementation = BindExceptionResponse::class))
                ]
            )
        ]
    )
    fun registerNickname(
        @Valid
        @RequestBody
        @Parameter(description = "등록할 닉네임", schema = Schema(implementation = RegisterNicknameRequest::class))
        request: RegisterNicknameRequest,

        @RequestHeader("X-User-ID")
        @Parameter(hidden = true)
        userID: Long): ResponseEntity<*>{
        try {
            service.registerNickname(request.nickname, userID)
        }catch (e: IllegalStateException){
            log.info("{userID: $userID}", e)
            return ResponseEntity(ExceptionResponse(404, "닉네임 등록에 에러가 발생했습니다.", e.message), HttpStatus.NOT_FOUND)
        }

        return ResponseEntity.noContent().build<Void>()
    }

}
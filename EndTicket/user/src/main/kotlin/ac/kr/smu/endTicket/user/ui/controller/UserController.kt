package ac.kr.smu.endTicket.user.ui.controller

import ac.kr.smu.endTicket.infra.client.AuthClient
import ac.kr.smu.endTicket.user.domain.model.User
import ac.kr.smu.endTicket.user.domain.service.UserService
import ac.kr.smu.endTicket.user.ui.request.CreateUserRequest
import ac.kr.smu.endTicket.user.ui.response.BindingExceptionResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.headers.Header
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.validation.BindException
import org.springframework.validation.BindingResult
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/users")
class UserController(
    private val userService: UserService,
    private val authClient: AuthClient
) {
    @PostMapping
    @Operation(summary = "회원가입", description = "사용자 회원 가입")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "201", description = "회원가입 완료"),
            ApiResponse(responseCode = "400", description = "필드 검증 실패", content = [
                Content(schema = Schema(implementation = BindingExceptionResponse::class))
            ]),
        ]
    )
    fun createUser(
        @Valid
        @RequestBody
        @Parameter(description = "회원가입 할 사용자 정보", required = true)
        userRequest: CreateUserRequest): ResponseEntity<Void>{
        val socialUserNumber = authClient.getSocialUserNumber(userRequest.code)

        userService.createUser(User(userRequest.nickname, userRequest.email, userRequest.socialType, socialUserNumber))

        return ResponseEntity.status(201).build()
    }

    @GetMapping("/{email}")
    @Operation(summary = "이메일 중복 확인", description = "회원 이메일이 중복되었는지 확인합니다.")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "해당 이메일의 사용자가 존재할 시"),
            ApiResponse(responseCode = "404", description = "해당 이메일의 사용자가 존재하지 않을 시")
        ]
    )
    fun checkUserExistenceByEmail(@PathVariable("email") @Parameter(description = "회원 이메일", required = true, example = "example@gmail.com")
                                  email: String): ResponseEntity<Void>{
        return if (userService.checkUserExistenceByEmail(email)) ResponseEntity.ok().build() else ResponseEntity.notFound().build()
    }


    @ExceptionHandler(BindException::class)
    fun handleBindingException(e: BindException, bindingResult: BindingResult): ResponseEntity<*>{
        return ResponseEntity.badRequest().body(
            BindingExceptionResponse(
                bindingResult.fieldError?.field,
                bindingResult.fieldError?.defaultMessage
            )
        )
    }
}
package ac.kr.smu.endTicket.user.ui.controller

import ac.kr.smu.endTicket.user.domain.exception.UserAlreadyExistException
import ac.kr.smu.endTicket.user.domain.model.User
import ac.kr.smu.endTicket.user.domain.service.UserService
import ac.kr.smu.endTicket.user.ui.request.CreateUserRequest
import ac.kr.smu.endTicket.user.ui.response.BindingExceptionResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.media.SchemaProperty
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
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/users")
class UserController(
    private val userService: UserService
) {
    @PostMapping
    @Operation(summary = "회원가입", description = "사용자 회원 가입")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "201", description = "회원가입 완료"),
            ApiResponse(responseCode = "400", description = "필드 검증 실패", content = [
                Content(schema = Schema(implementation = BindingExceptionResponse::class))
            ]),
            ApiResponse(responseCode = "409", description = "이미 회원가입 되어 있는 사용자")
        ]
    )
    fun createUser(
        @Valid
        @RequestBody
        @Parameter(description = "회원가입 할 사용자 정보", required = true)
        userRequest: CreateUserRequest): ResponseEntity<Void>{
        try{
            userService.createUser(User(userRequest.nickname, userRequest.socialType, userRequest.socialUserNumber))
        }catch (e: UserAlreadyExistException){
            return ResponseEntity.status(409).build()
        }

        return ResponseEntity.status(201).build()
    }



    @GetMapping("/{socialType}/{socialUserNumber}")
    @Operation(summary = "사용자 조회", description = "SNS 사용자 번호를 가진 사용자 조회")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "SNS 사용자 번호를 가진 사용자가 존재할 시", content = [Content(schema = Schema(name = "SNS 사용자 번호", type = "object", requiredProperties = ["socialUserNumber"]), schemaProperties = [SchemaProperty(name = "userId", schema = Schema(type = "long", example = "1"))])]),
            ApiResponse(responseCode = "404", description = "SNS 사용자 번호의 사용자가 없을 시")
        ]
    )
    fun getUserId(
        @Parameter(description = "가입한 SNS", required = true)
        @PathVariable("socialType")
        socialType: User.SocialType,
        @Parameter(description = "SNS 사용자 번호", required = true)
        @PathVariable("socialUserNumber") socialUserNumber: Long): ResponseEntity<*>{
        val socialUserNumber = userService.findBySocialTypeAndSocialUserNumber(socialType, socialUserNumber) ?: return ResponseEntity.notFound().build<Void>()

        return ResponseEntity.ok(mapOf("socialUserNumber" to socialUserNumber))
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
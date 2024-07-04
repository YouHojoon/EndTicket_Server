package ac.kr.smu.endticket.user.ui.controller

import ac.kr.smu.endticket.common.constant.HttpHeaderName
import ac.kr.smu.endticket.user.service.UserService
import ac.kr.smu.endticket.user.ui.request.NicknameRegisterRequest

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
import ac.kr.smu.endticket.common.web.response.BindExceptionResponse
import ac.kr.smu.endticket.common.web.response.ExceptionResponse

import ac.kr.smu.endticket.user.domain.exception.UserNotFoundException
import ac.kr.smu.endticket.user.swagger.apiresponses.DeleteUserApiResponses
import ac.kr.smu.endticket.user.swagger.apiresponses.FindNicknameApiResponses
import ac.kr.smu.endticket.user.swagger.apiresponses.RegisterNicknameApiResponses
import brave.Response
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.GetMapping

@RestController
@RequestMapping("/users")
@Tag(name = "/users")
@SecurityRequirement(name = "Access token")
class  UserController(
    private val service: UserService
) {
    private val log = LoggerFactory.getLogger(UserController::class.java)

    @RegisterNicknameApiResponses
    @PostMapping("nickname")
    fun registerNickname(
        @Valid
        @RequestBody
        @Parameter(description = "등록할 닉네임", schema = Schema(implementation = NicknameRegisterRequest::class))
        request: NicknameRegisterRequest,

        @RequestHeader(HttpHeaderName.USER_ID)
        @Parameter(hidden = true)
        id: Long): ResponseEntity<*> =
        try {
            service.registerNickname(request, id)
            ResponseEntity.noContent().build<Void>()
        }catch (e: IllegalStateException){
            log.info("{id: $id}", e)
            ResponseEntity(ExceptionResponse(409, "닉네임 등록에 에러가 발생했습니다.", e.message), HttpStatus.CONFLICT)
        }
        catch (e: UserNotFoundException){
            log.info("{id: $id}", e)
            ResponseEntity(ExceptionResponse(404, "닉네임 등록에 에러가 발생했습니다.", e.message), HttpStatus.NOT_FOUND)
        }

    @FindNicknameApiResponses
    @GetMapping("nickname")
    fun findNickname(
        @RequestHeader(HttpHeaderName.USER_ID)
        @Schema(hidden = true)
        id: Long
    ) = ResponseEntity.ok(mapOf("nickname" to service.findNickname(id)))

    @DeleteUserApiResponses
    @DeleteMapping
    fun deleteUser(
        @RequestHeader(HttpHeaderName.USER_ID)
        @Parameter(hidden = true)
        id: Long
    ): ResponseEntity<Void>{
        service.deleteUser(id)
        return ResponseEntity.noContent().build()
    }

    @ExceptionHandler(UserNotFoundException::class)
    fun handleUserNotFoundException(e: UserNotFoundException): ResponseEntity<ExceptionResponse> {
        log.info("{id: ${e.id}}", e)
        val status = HttpStatus.NOT_FOUND
        return ResponseEntity(ExceptionResponse(status.value(), "닉네임 등록에 에러가 발생했습니다.", e.message),status)
    }
}
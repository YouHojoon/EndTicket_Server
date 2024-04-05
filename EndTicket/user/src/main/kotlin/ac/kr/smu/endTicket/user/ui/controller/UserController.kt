package ac.kr.smu.endTicket.user.ui.controller

import ac.kr.smu.endTicket.user.domain.service.UserService
import ac.kr.smu.endTicket.user.ui.response.BindingExceptionResponse
import org.springframework.http.ResponseEntity
import org.springframework.validation.BindException
import org.springframework.validation.BindingResult
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/users")
class UserController(
    private val userService: UserService
) {
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
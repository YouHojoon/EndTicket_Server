package aop

import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.validation.BindException
import org.springframework.validation.BindingResult
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import response.BindingExceptionResponse

@ControllerAdvice
class BindingExceptionAdvice {
    private val log = LoggerFactory.getLogger(BindingExceptionAdvice::class.java)

    @ExceptionHandler(BindException::class)
    fun handleBindingException(e: BindException, bindingResult: BindingResult): ResponseEntity<*>{
        log.info(
            "{field: ${e.bindingResult.fieldError?.field}, objectName: ${e.bindingResult.objectName}, rejectedValue: ${e.bindingResult.fieldError?.rejectedValue}}",
            e
        )

        return ResponseEntity.badRequest().body(
            BindingExceptionResponse(
                field = e.bindingResult.fieldError?.field,
                code = 400,
                objectName = e.bindingResult.objectName,
                detail = e.bindingResult.fieldError?.defaultMessage
            )
        )
    }
}
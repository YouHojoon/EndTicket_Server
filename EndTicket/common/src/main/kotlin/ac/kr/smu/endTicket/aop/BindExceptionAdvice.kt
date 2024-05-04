package ac.kr.smu.endTicket.aop

import org.slf4j.LoggerFactory
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatusCode
import org.springframework.http.ResponseEntity
import org.springframework.validation.BindException
import org.springframework.validation.BindingResult
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.context.request.WebRequest
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler
import ac.kr.smu.endTicket.response.BindExceptionResponse

/**
 * [BindException]을 공통적으로 처리하는 클래스
 */
@ControllerAdvice
class BindExceptionAdvice {
    private val log = LoggerFactory.getLogger(BindException::class.java)

    /**
     * [BindException]을 처리하는 메소드
     * @param e 발생한 에러
     * @param bindingResult binding의 결과
     * @return 에러에 대한 내용을 반환한다. [BindException]
     */
    @ExceptionHandler(BindException::class)
    fun handleBindingException(e: BindException, bindingResult: BindingResult): ResponseEntity<*>{
        log.info(
            "{field: ${e.bindingResult.fieldError?.field}, objectName: ${e.bindingResult.objectName}, rejectedValue: ${e.bindingResult.fieldError?.rejectedValue}}",
            e
        )

        return ResponseEntity.badRequest().body(
            BindExceptionResponse(
                field = e.bindingResult.fieldError?.field,
                code = 400,
                objectName = e.bindingResult.objectName,
                detail = e.bindingResult.fieldError?.defaultMessage
            )
        )
    }
}
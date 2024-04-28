package aop

import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.AfterThrowing
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Component
import org.springframework.validation.BindException
import org.springframework.validation.BindingResult
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestController
import response.BindingExceptionResponse
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.hasAnnotation

@Component
@Aspect
class AutoBindingExceptionHandleAspect {
    private val log = LoggerFactory.getLogger(AutoBindingExceptionHandleAspect::class.java)

    @Around("@annotation(annotation.AutoBindingExceptionHandle)")
    fun handleBindingException(joinPoint: ProceedingJoinPoint): Any? {
        if (!joinPoint.target::class.hasAnnotation<RestController>())
            throw IllegalStateException("@AutoBindingException은 @RestContoller에서만 사용할 수 있습니다.")

        try {
            return joinPoint.proceed()
        }catch (e: BindException){
            log.info("{field: ${e.bindingResult.fieldError?.field}, objectName: ${e.bindingResult.objectName}, rejectedValue: ${e.bindingResult.fieldError?.rejectedValue}}", e)

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
}
package ac.kr.smu.endTicket.futureMe.ui.controller

import ac.kr.smu.endTicket.constant.HttpHeaderName
import ac.kr.smu.endTicket.futureMe.domain.futureMe.exception.NotFoundFutureMeException
import ac.kr.smu.endTicket.futureMe.domain.futureMe.model.Character
import ac.kr.smu.endTicket.futureMe.service.FutureMeService
import ac.kr.smu.endTicket.response.ExceptionResponse
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.core.io.Resource
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer.HeaderNames
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/future-me")
@Tag(name = "/future-me")
class FutureMeController(
    private val service: FutureMeService
) {

    @GetMapping
    fun findFutureMe(@RequestHeader(HttpHeaderName.USER_ID) userID: Long) = ResponseEntity.ok().body(service.findFutureMe(userID))
    @GetMapping("characters/{type}")
    fun findCharacterImage(@PathVariable("type") type: Character.Type) =
        ResponseEntity
            .ok()
            .contentType(MediaType.valueOf("image/svg+xml"))
            .body(type.imageResource)

    @ExceptionHandler(NotFoundFutureMeException::class)
    fun handleNotFoundFutureMeException(e: NotFoundFutureMeException) =
        ResponseEntity.status(HttpStatus.NOT_FOUND).body(ExceptionResponse(
            code = 404,
            message = "미래의 나 조회에 에러가 발생했습니다.",
            detail = e.message
        ))
}
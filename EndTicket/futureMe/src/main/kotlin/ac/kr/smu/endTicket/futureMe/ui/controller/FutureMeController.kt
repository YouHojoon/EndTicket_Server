package ac.kr.smu.endTicket.futureMe.ui.controller

import ac.kr.smu.endTicket.common.web.response.ExceptionResponse
import ac.kr.smu.endTicket.constant.HttpHeaderName
import ac.kr.smu.endTicket.futureMe.domain.futureMe.exception.FutureMeNotFoundException
import ac.kr.smu.endTicket.futureMe.domain.futureMe.exception.UnsupportedCharacterException
import ac.kr.smu.endTicket.futureMe.domain.futureMe.model.Character
import ac.kr.smu.endTicket.futureMe.infra.swagger.apiResponses.futureMe.*
import ac.kr.smu.endTicket.futureMe.service.FutureMeService
import ac.kr.smu.endTicket.futureMe.ui.request.CreateFutureMeRequest
import ac.kr.smu.endTicket.futureMe.ui.request.UpdateFutureMeRequest
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/future-me")
@Tag(name = "/future-me")
@SecurityRequirement(name = "Access token")
class FutureMeController(
    private val service: FutureMeService
) {
    private val log = LoggerFactory.getLogger(FutureMeController::class.java)

    @GetMapping
    @FindFutureMeApiResponses
    fun findFutureMe(
        @Parameter(hidden = true)
        @RequestHeader(HttpHeaderName.USER_ID)
        userID: Long
    ) = ResponseEntity.ok().body(service.findFutureMe(userID))

    @GetMapping("characters/{type}")
    @FindCharacterImageResponses
    fun findCharacterImage(
        @Parameter(
            name = "캐릭터의 타입",
            schema = Schema(implementation = Character.Type::class),
            required = true
        )
        @PathVariable("type")
        type: Character.Type
    ) =  ResponseEntity
                .ok()
                .contentType(MediaType.valueOf("image/svg+xml"))
                .body(type.imageResource)

    @PostMapping
    @CreateFutureMeApiResponses
    fun createFutureMe(
        @Parameter(
            name = "캐릭터의 타입",
            schema = Schema(implementation = CreateFutureMeRequest::class),
            required = true
        )
        @RequestBody
        request: CreateFutureMeRequest,

        @Parameter(hidden = true)
        @RequestHeader(HttpHeaderName.USER_ID)
        userID: Long
    ) = try{
        ResponseEntity.status(HttpStatus.CREATED).body(service.createFutureMe(request,userID))
    }catch (e: IllegalStateException){
        log.info("uesrID: $userID", e)

        val status = HttpStatus.CONFLICT
        ResponseEntity.status(status).body(ExceptionResponse(
            code = status.value(),
            message = "미래의 나 생성 중 에러가 발생했습니다.",
            detail = e.message
        ))
    }

    @PatchMapping
    @UpdateFutureMeApiResponses
    fun updateTitle(
        @Parameter(
            description = "미래의 나 수정 요청",
            schema = Schema(implementation = UpdateFutureMeRequest::class),
            required = true
        )
        @RequestBody
        @Valid
        request: UpdateFutureMeRequest,

        @RequestHeader(HttpHeaderName.USER_ID)
        @Parameter(hidden = true)
        userID: Long
    ) = ResponseEntity.ok(service.update(request,userID))

    @ExceptionHandler(FutureMeNotFoundException::class)
    fun handleNotFoundFutureMeException(e: FutureMeNotFoundException): ResponseEntity<ExceptionResponse>{
        val status = HttpStatus.NOT_FOUND
        log.info("{userID: ${e.userID}}", e)

        return ResponseEntity.status(status).body(
            ExceptionResponse(
                code = status.value(),
                message = "미래의 나 조회에 에러가 발생했습니다.",
                detail = e.message
            ))
    }

    @ExceptionHandler(UnsupportedCharacterException::class)
    fun handleMethodArgumentTypeMismatchException(e: UnsupportedCharacterException): ResponseEntity<ExceptionResponse>{
        val status = HttpStatus.BAD_REQUEST
        log.info("{input: ${e.input}}",e)

        return ResponseEntity
            .status(status)
            .body(ExceptionResponse(
                code = status.value(),
                message = "캐릭터 조회 중 에러가 발생했습니다.",
                detail = e.message
            ))
    }

}
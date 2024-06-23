package ac.kr.smu.endticket.futureme.ui.controller

import ac.kr.smu.endticket.common.web.response.ExceptionResponse
import ac.kr.smu.endticket.common.constant.HttpHeaderName
import ac.kr.smu.endticket.common.web.enum.CharacterType
import ac.kr.smu.endticket.futureme.service.FutureMeService
import ac.kr.smu.endticket.futureme.ui.request.CreateFutureMeRequest
import ac.kr.smu.endticket.futureme.domain.futureme.exception.FutureMeNotFoundException
import ac.kr.smu.endticket.futureme.domain.futureme.exception.UnsupportedCharacterException
import ac.kr.smu.endticket.futureme.swagger.apiresponses.futureMe.CreateFutureMeApiResponses
import ac.kr.smu.endticket.futureme.swagger.apiresponses.futureMe.FindCharacterImageResponses
import ac.kr.smu.endticket.futureme.swagger.apiresponses.futureMe.FindFutureMeApiResponses
import ac.kr.smu.endticket.futureme.swagger.apiresponses.futureMe.UpdateFutureMeApiResponses
import ac.kr.smu.endticket.futureme.ui.request.UpdateFutureMeRequest
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
import ac.kr.smu.endticket.futureme.domain.futureme.model.Character

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
        userId: Long
    ) = ResponseEntity.ok().body(service.findFutureMe(userId))

    @GetMapping("characters/{type}")
    @FindCharacterImageResponses
    fun findCharacterImage(
        @Parameter(
            name = "캐릭터의 타입",
            schema = Schema(implementation = CharacterType::class),
            required = true
        )
        @PathVariable("type")
        type: CharacterType
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
        userId: Long
    ) = try{
        ResponseEntity.status(HttpStatus.CREATED).body(service.createFutureMe(request,userId))
    }catch (e: IllegalStateException){
        log.info("uesrId: $userId", e)

        val status = HttpStatus.CONFLICT
        ResponseEntity.status(status).body(
            ExceptionResponse(
            code = status.value(),
            message = "미래의 나 생성 중 에러가 발생했습니다.",
            detail = e.message
        )
        )
    }

    @PatchMapping
    @UpdateFutureMeApiResponses
    fun updateFutureMe(
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
        userId: Long
    ) = ResponseEntity.ok(service.updateFutureMe(request,userId))

    @ExceptionHandler(FutureMeNotFoundException::class)
    fun handleNotFoundFutureMeException(e: FutureMeNotFoundException): ResponseEntity<ExceptionResponse>{
        val status = HttpStatus.NOT_FOUND
        log.info("{userId: ${e.userId}}", e)

        return ResponseEntity.status(status).body(
            ExceptionResponse(
                code = status.value(),
                message = "미래의 나 조회에 에러가 발생했습니다.",
                detail = e.message
            )
        )
    }

    @ExceptionHandler(UnsupportedCharacterException::class)
    fun handleMethodArgumentTypeMismatchException(e: UnsupportedCharacterException): ResponseEntity<ExceptionResponse>{
        val status = HttpStatus.BAD_REQUEST
        log.info("{input: ${e.input}}",e)

        return ResponseEntity
            .status(status)
            .body(
                ExceptionResponse(
                code = status.value(),
                message = "캐릭터 조회 중 에러가 발생했습니다.",
                detail = e.message
            )
            )
    }

}
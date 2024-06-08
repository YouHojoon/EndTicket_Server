package ac.kr.smu.endTicket.futureMe.ui.controller

import ac.kr.smu.endTicket.constant.HttpHeaderName
import ac.kr.smu.endTicket.futureMe.domain.futureMe.exception.NotFoundFutureMeException
import ac.kr.smu.endTicket.futureMe.domain.futureMe.model.Character
import ac.kr.smu.endTicket.futureMe.domain.futureMe.model.FutureMe
import ac.kr.smu.endTicket.futureMe.service.FutureMeService
import ac.kr.smu.endTicket.response.ExceptionResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
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
@SecurityRequirement(name = "Access token")
class FutureMeController(
    private val service: FutureMeService
) {
    @GetMapping
    @Operation(description = "미래의 나 조회")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "조회 성공",
                content = [
                    Content(schema = Schema(implementation = FutureMe::class))
                ]),
            ApiResponse(
                responseCode = "404",
                description = "미래의 나가 존재하지 않음",
                content = [
                    Content(schema = Schema(implementation = ExceptionResponse::class))
                ]
            )
        ]
    )
    fun findFutureMe(
        @Parameter(hidden = true)
        @RequestHeader(HttpHeaderName.USER_ID)
        userID: Long
    ) = ResponseEntity.ok().body(service.findFutureMe(userID))

    @GetMapping("characters/{type}")
    @Operation(description = "캐릭터 이미지 조회")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "조회 성공",
                content = [
                Content(
                    mediaType = "image/svg+xml",
                    schema = Schema(type = "string", format = "binary")
                )
            ]),
            ApiResponse(
                responseCode = "404",
                description = "존재하지 않는 캐릭터",
                content = [
                    Content(schema = Schema(implementation = ExceptionResponse::class))
                ]
            )
        ]
    )
    fun findCharacterImage(@PathVariable("type") type: Character.Type) =
        ResponseEntity
            .ok()
            .contentType(MediaType.valueOf("image/svg+xml"))
            .body(type.imageResource)

    @ExceptionHandler(NotFoundFutureMeException::class)
    fun handleNotFoundFutureMeException(e: NotFoundFutureMeException) =
        ResponseEntity.status(HttpStatus.NOT_FOUND).body(
            ExceptionResponse(
            code = 404,
            message = "미래의 나 조회에 에러가 발생했습니다.",
            detail = e.message
        ))
}
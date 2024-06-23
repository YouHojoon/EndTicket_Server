package ac.kr.smu.endticket.futureme.ui.controller

import ac.kr.smu.endticket.futureme.swagger.apiresponses.imagination.UpdateImaginationApiResponses
import ac.kr.smu.endticket.common.constant.HttpHeaderName
import ac.kr.smu.endticket.common.web.response.ExceptionResponse
import ac.kr.smu.endticket.futureme.domain.imagination.exception.ImaginationNotFoundException
import ac.kr.smu.endticket.futureme.domain.imagination.exception.ImaginationOwnershipException
import ac.kr.smu.endticket.futureme.service.ImaginationService
import ac.kr.smu.endticket.futureme.swagger.apiresponses.imagination.CompleteImaginationApiResponses
import ac.kr.smu.endticket.futureme.swagger.apiresponses.imagination.CreateImaginationApiResponses
import ac.kr.smu.endticket.futureme.swagger.apiresponses.imagination.DeleteImaginationApiResponses
import ac.kr.smu.endticket.futureme.swagger.apiresponses.imagination.FindImaginationsApiResponses
import ac.kr.smu.endticket.futureme.ui.request.ImaginationRequest
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/imaginations")
@Tag(name = "/imaginations")
@SecurityRequirement(name = "Access token")
class ImaginationController(
    private val service: ImaginationService
) {
    private val log = LoggerFactory.getLogger(ImaginationController::class.java)

    @GetMapping
    @FindImaginationsApiResponses
    fun findImaginations(
        @RequestHeader(HttpHeaderName.USER_ID)
        @Parameter(hidden = true)
        userId: Long
    ) = ResponseEntity.ok(
        mapOf("imaginations" to service.findImaginations(userId))
    )

    @PostMapping
    @CreateImaginationApiResponses
    fun createImagination(
        @RequestBody
        @Parameter(
            description = "생성 요청",
            schema = Schema(implementation = ImaginationRequest::class),
            required = true
        )
        @Valid
        request: ImaginationRequest,

        @RequestHeader(HttpHeaderName.USER_ID)
        @Parameter(hidden = true)
        userId: Long
    ) =
        try {
            ResponseEntity
                .status(HttpStatus.CREATED)
                .body(service.createImagination(request,userId))
        }catch (e: IllegalStateException){
            log.info("{userId: $userId}",e)
            val status = HttpStatus.CONFLICT
            ResponseEntity
                .status(status)
                .body(
                    ExceptionResponse(
                        code = status.value(),
                        message = "미래의 나 생성에서 에러가 발생했습니다.",
                        detail = e.message
                    )
                )
        }

    @PutMapping("{id}")
    @UpdateImaginationApiResponses
    fun updateImagination(
        @PathVariable("id")
        @Parameter(
            description = "상상해보기 id",
            required = true,
            example = "1"
        )
        id: Long,

        @RequestBody
        @Parameter(
            description = "수정 요청",
            required = true,
            schema = Schema(implementation = ImaginationRequest::class)
        )
        @Valid
        request: ImaginationRequest,

        @RequestHeader(HttpHeaderName.USER_ID)
        @Parameter(hidden = true)
        userId: Long
    ) = ResponseEntity.ok(service.updateImagination(request, id, userId))

    @DeleteMapping("{id}")
    @DeleteImaginationApiResponses
    fun deleteImagination(
        @PathVariable("id")
        @Parameter(
            description = "상상해보기 id",
            example = "1",
            required = true
        )
        id: Long,

        @RequestHeader(HttpHeaderName.USER_ID)
        @Parameter(hidden = true)
        userId: Long
    ): ResponseEntity<Void>{
        service.deleteImagination(id,userId)
        return ResponseEntity.noContent().build()
    }

    @PostMapping("complete/{id}")
    @CompleteImaginationApiResponses
    fun completeImagination(
        @PathVariable("id")
        id: Long,

        @RequestHeader(HttpHeaderName.USER_ID)
        @Parameter(hidden = true)
        userId: Long
    ): ResponseEntity<Void>{
        service.completeImagination(id, userId)
        return ResponseEntity.noContent().build()
    }

    @ExceptionHandler(ImaginationNotFoundException::class)
    fun handleNotFoundImaginationException(e: ImaginationNotFoundException): ResponseEntity<ExceptionResponse>{
        log.info("${e.id}",e)
        val status = HttpStatus.NOT_FOUND

        return ResponseEntity.status(status).body(
            ExceptionResponse(
                code = status.value(),
                message = "상상해보기 조회에 에러가 발생했습니다.",
                detail = e.message
            )
        )
    }

    @ExceptionHandler(ImaginationOwnershipException::class)
    fun handleNotOwnerOfImaginationException(e: ImaginationOwnershipException): ResponseEntity<ExceptionResponse>{
        log.info("{id: ${e.id}, userId: ${e.userId}}",e)
        val status = HttpStatus.FORBIDDEN

        return ResponseEntity.status(status).body(
            ExceptionResponse(
                code = status.value(),
                message = "상상해보기 요청 중 에러가 발생했습니다.",
                detail = e.message
            )
        )
    }
}
package ac.kr.smu.endticket.history.ui.controller

import ac.kr.smu.endticket.common.constant.HttpHeaderName
import ac.kr.smu.endticket.history.domain.model.History
import ac.kr.smu.endticket.history.service.HistoryService
import ac.kr.smu.endticket.history.swagger.apiresponses.FindHistoriesApiResponses
import ac.kr.smu.endticket.history.swagger.apiresponses.FindHistoryCountApiResponses
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableDefault
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/histories")
@Tag(name = "/histories")
@SecurityRequirement(name = "Access token")
class HistoryController(
    private val service: HistoryService,
) {
    @FindHistoriesApiResponses
    @GetMapping("{type}")
    fun findHistories(
        @PathVariable("type")
        @Parameter(description = "기록의 종류", schema = Schema(implementation = History.Type::class))
        type: History.Type,
        @PageableDefault
        @Parameter(description = "조회할 페이지", schema = Schema(implementation = Pageable::class))
        pageable: Pageable,
        @Parameter(hidden = true)
        @RequestHeader(HttpHeaderName.USER_ID)
        userId: Long,
    ) = ResponseEntity.ok(service.findHistories(userId, type, pageable))

    @FindHistoryCountApiResponses
    @GetMapping("/count")
    fun findHistoryCount(
        @Parameter(hidden = true)
        @RequestHeader(HttpHeaderName.USER_ID)
        userId: Long,
    ) = ResponseEntity.ok(service.findHistoryCount(userId))
}

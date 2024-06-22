package ac.kr.smu.endticket.history.ui.controller

import ac.kr.smu.endticket.common.constant.HttpHeaderName
import ac.kr.smu.endticket.history.domain.model.History
import ac.kr.smu.endticket.history.service.HistoryService
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableDefault
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/histories")
class HistoryController(
    private val service: HistoryService
) {
    @GetMapping("{type}")
    fun findHistories(
        @PathVariable("type")
        type: History.Type,

        @PageableDefault
        pageable: Pageable,

        @RequestHeader(HttpHeaderName.USER_ID)
        userId: Long
    ) = ResponseEntity.ok(service.findHistories(userId, type, pageable))
}
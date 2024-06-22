package ac.kr.smu.endticket.history.service

import ac.kr.smu.endticket.history.domain.model.History
import ac.kr.smu.endticket.history.domain.model.HistorySlice
import ac.kr.smu.endticket.history.domain.repository.HistoryRepository
import ac.kr.smu.endticket.history.ui.response.HistoryResponse
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Slice
import org.springframework.stereotype.Service

@Service
class HistoryService(
    private val repo: HistoryRepository
) {
    fun findHistories(userId: Long, type: History.Type, pageable: Pageable): HistorySlice<HistoryResponse> =
        repo.findAllByUserIdAndType(userId, type, pageable).map(History::toResponse)

}
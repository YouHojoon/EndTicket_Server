package ac.kr.smu.endticket.history.service

import ac.kr.smu.endticket.history.domain.model.History
import ac.kr.smu.endticket.history.ui.response.HistorySlice
import ac.kr.smu.endticket.history.domain.repository.HistoryRepository
import ac.kr.smu.endticket.history.ui.response.HistoryResponse
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service

@Service
class HistoryService(
    private val repo: HistoryRepository
) {
    /**
     * 사용자의 기록들을 조회하는 메소드
     * @param userId 사용자 id
     * @param type 조회할 기록 종류
     * @param pageable 조회할 페잊
     * @return 조회된 기록들
     */
    fun findHistories(userId: Long, type: History.Type, pageable: Pageable): HistorySlice<HistoryResponse> =
        repo.findAllByUserIdAndType(userId, type, pageable).map(History::toResponse)
}
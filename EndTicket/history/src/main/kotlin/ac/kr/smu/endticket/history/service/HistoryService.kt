package ac.kr.smu.endticket.history.service

import ac.kr.smu.endticket.history.domain.model.History
import ac.kr.smu.endticket.history.domain.repository.HistoryRepository
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Slice
import org.springframework.stereotype.Service
import kotlin.reflect.KClass

@Service
class HistoryService(
    private val repo: HistoryRepository
) {
    fun findHistories(userId: Long, type: KClass<out History>, pageable: Pageable): Slice<out History> =
        repo.findAllByUserIdAndType(userId, type, pageable)

}
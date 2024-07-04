package ac.kr.smu.endticket.history.domain.repository

import ac.kr.smu.endticket.history.domain.model.History
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface HistoryRepository: JpaRepository<History, Long>, HistorySupport{
    fun deleteByUserId(userId:Long)
}

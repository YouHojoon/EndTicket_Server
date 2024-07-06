package ac.kr.smu.endticket.user.domain.repository

import ac.kr.smu.endticket.user.domain.model.UserDeletedEvent
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
interface UserDeletedEventRepository : JpaRepository<UserDeletedEvent, Long> {
    fun findByAuditCreatedAtBefore(date: LocalDateTime): Set<UserDeletedEvent>
}

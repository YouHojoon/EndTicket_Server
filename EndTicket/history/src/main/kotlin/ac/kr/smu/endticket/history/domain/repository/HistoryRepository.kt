package ac.kr.smu.endticket.history.domain.repository

import ac.kr.smu.endticket.history.constant.RedisConstant
import ac.kr.smu.endticket.history.domain.model.History
import org.springframework.cache.annotation.CacheEvict
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface HistoryRepository :
    JpaRepository<History, Long>,
    HistorySupport {
    /**
     * 사용자의 기록을 삭제하는 메소드, 캐시의 사용자 기록 개수도 삭제된다.
     * @param userId 사용자 id
     */
    @Modifying
    @CacheEvict(RedisConstant.HISTORY_COUNT_REDIS_PREFIX, key = "#userId")
    @Query("DELETE FROM History h WHERE h.userId = :userId")
    fun deleteByUserId(userId: Long)
}

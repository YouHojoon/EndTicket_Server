package ac.kr.smu.endticket.futureme.domain.imagination.repository

import ac.kr.smu.endticket.futureme.domain.imagination.model.Imagination
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ImaginationRepository : JpaRepository<Imagination, Long> {
    /**
     * 미완료된 상상해보기 개수 조회 메소드
     * @param userId 사용자 Id
     * @return 미완료된 상상해보기 개수
     */
    fun countByUserIdAndIsCompleteIsFalse(userId: Long): Int

    /**
     * 미완료된 상상해보기 조회 메소드
     * @param userId 사용자 Id
     * @return 미완료된 상상해보기
     */
    fun findByUserIdAndIsCompleteIsFalse(userId: Long): Set<Imagination>

    fun deleteByUserId(userId: Long)
}

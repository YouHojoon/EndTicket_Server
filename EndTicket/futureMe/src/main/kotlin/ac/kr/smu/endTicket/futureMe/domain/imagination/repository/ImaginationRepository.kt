package ac.kr.smu.endTicket.futureMe.domain.imagination.repository

import ac.kr.smu.endTicket.futureMe.domain.imagination.model.Imagination
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ImaginationRepository: JpaRepository<Imagination, Long>{
    /**
     * 미완료된 상상해보기 개수 조회 메소드
     * @param userID 사용자 ID
     * @return 미완료된 상상해보기 개수
     */
    fun countByUserIDAndIsCompleteIsFalse(userID: Long): Int

    /**
     * 미완료된 상상해보기 조회 메소드
     * @param userID 사용자 ID
     * @return 미완료된 상상해보기
     */
    fun findByUserIDAndIsCompleteIsFalse(userID: Long): Set<Imagination>
}
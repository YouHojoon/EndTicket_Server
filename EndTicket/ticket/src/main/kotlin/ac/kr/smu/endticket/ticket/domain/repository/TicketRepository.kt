package ac.kr.smu.endticket.ticket.domain.repository

import ac.kr.smu.endticket.ticket.domain.model.Ticket
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface TicketRepository : JpaRepository<Ticket, Long> {
    /**
     * 사용자의 미완료된 티켓을 조회하는 메소드
     * @param userId 사용자의 Id
     * @return 조회된 티켓 리스트
     */
    @Query("select t from Ticket as t where t.swipeCount < t.maxSwipeCount and t.userId = :userId")
    fun findIncompleteTicketsOfUser(userId: Long): List<Ticket>

    /**
     * 사용자의 티캣 개수를 조회하는 메소드
     * @param userId 사용자의 Id
     */
    @Query("select count(t) from Ticket as t where t.swipeCount < t.maxSwipeCount and t.userId = :userId")
    fun countIncompleteTicketsOfUser(userId: Long): Int

    fun deleteByUserId(userId: Long)
}

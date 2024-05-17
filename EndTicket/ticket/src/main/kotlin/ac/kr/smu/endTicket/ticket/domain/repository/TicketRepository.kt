package ac.kr.smu.endTicket.ticket.domain.repository

import ac.kr.smu.endTicket.ticket.domain.model.Ticket
import org.springframework.cache.annotation.Cacheable

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.util.Optional

@Repository
interface TicketRepository: JpaRepository<Ticket, Long> {
    @Cacheable(cacheNames = ["ticket"], key = "#id")
    override fun findById(id: Long): Optional<Ticket>

    /**
     * 사용자의 미완료된 티켓을 조회하는 메소드
     * @param userID 사용자의 ID
     * @return 조회된 티켓 리스트
     */
    @Query("select t from Ticket as t where t.swipeCount < t.maxSwipeCount and t.userID = :userID")
    fun findIncompleteTicketsOfUser(userID: Long): List<Ticket>

    /**
     * 사용자의 티캣 개수를 조회하는 메소드
     * @param userID 사용자의 ID
     */
    fun countByUserID(userID: Long): Int
}
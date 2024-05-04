package ac.kr.smu.endTicket.ticket.service

import ac.kr.smu.endTicket.ticket.domain.exception.NotOwnerOfTicketException
import ac.kr.smu.endTicket.ticket.domain.model.Ticket
import ac.kr.smu.endTicket.ticket.domain.repository.TicketRepository
import ac.kr.smu.endTicket.ticket.ui.request.TicketRequest
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import kotlin.jvm.optionals.getOrNull

/**
 * 티켓 관련한 기능을 처리하는 클래스
 */
@Service
class TicketService(
    private val repo: TicketRepository
) {
    /**
     * 티켓을 생성하는 메소드
     * @param request 티켓 생성에 대한 요청
     * @param userID 티켓 생성을 요청한 user의 ID
     * @return 생성된 티켓
     */
    @Transactional
    fun createTicket(request: TicketRequest, userID: Long): Ticket{
        return repo.save(
            Ticket(request,userID)
        )
    }

    /**
     * 티켓을 수정하는 메소드
     * @param request 수정할 티켓 요청
     * @param id 티켓 id
     * @return 수정된 티켓
     * @throws IllegalStateException id로 조회한 티켓이 없을 시
     * @throws NotOwnerOfTicketException 사용자가 티켓의 소유자가 아닐
     */

    @Throws(IllegalStateException::class, NotOwnerOfTicketException::class)
    @Transactional
    fun updateTicket(request: TicketRequest, id: Long, userID: Long): Ticket{
        val old = repo.findById(id).getOrNull()

        checkNotNull(old){
            "id $id 의 티켓이 없습니다."
        }
        if (old.userID != userID)
            throw NotOwnerOfTicketException()

        old.update(request)

        return old
    }
}
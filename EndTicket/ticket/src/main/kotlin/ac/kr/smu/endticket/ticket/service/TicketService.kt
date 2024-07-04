package ac.kr.smu.endticket.ticket.service

import ac.kr.smu.endticket.ticket.domain.exception.TicketNotFoundException
import ac.kr.smu.endticket.ticket.domain.exception.TicketOwnershipException
import ac.kr.smu.endticket.ticket.domain.model.Ticket
import ac.kr.smu.endticket.ticket.domain.model.TicketCompletedEvent
import ac.kr.smu.endticket.ticket.domain.repository.TicketRepository
import ac.kr.smu.endticket.ticket.ui.request.TicketRequest
import ac.kr.smu.endticket.ticket.ui.response.TicketResponse
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * 티켓 관련한 기능을 처리하는 클래스
 * @property repo 티켓을 저장하기 위해 사용하는 저장소
 * @property completionEventService 이벤트를 전송하기 위한 서비스
 */
@Service
class TicketService(
    private val repo: TicketRepository,
    private val completionEventService: TicketCompletedEventService,
) {
    private companion object {
        private const val TICKET_LIMIT = 5
    }

    /**
     * 티켓을 생성하는 메소드
     * @param request 티켓 생성에 대한 요청
     * @param userId 티켓 생성을 요청한 user의 Id
     * @return 생성된 티켓 응답
     * @throws IllegalStateException 티켓 개수 제한 이상으로 생성을 시도할 시
     */
    @Transactional
    fun createTicket(
        request: TicketRequest,
        userId: Long,
    ): TicketResponse {
        val count = repo.countIncompleteTicketsOfUser(userId)

        check(count < TICKET_LIMIT) { "티켓을 $TICKET_LIMIT 개 이상 생성할 수 없습니다." }

        return repo.save(Ticket.from(request, userId)).toResponse()
    }

    /**
     * 티켓을 수정하는 메소드
     * @param request 수정할 티켓 요청
     * @param id 티켓 id
     * @param userId 티켓의 소유자 Id
     * @return 수정된 티켓 응답
     * @throws TicketNotFoundException id로 조회한 티켓이 없을 시
     * @throws TicketOwnershipException 사용자가 티켓의 소유자가 아닐 시
     */

    @Transactional
    fun updateTicket(
        request: TicketRequest,
        id: Long,
        userId: Long,
    ): TicketResponse {
        val ticket = findById(id)

        if (ticket.updateAndCheckCompletion(request, userId)) {
            completeTicket(ticket)
        }

        return ticket.toResponse()
    }

    /**
     * 티켓 스와이프를 처리하는 메소드
     * @param id 티켓의 id
     * @param userId 티켓의 소유자 Id
     * @return 스와이프 처리된 티켓 응답
     * @throws TicketNotFoundException id로 조회한 티켓이 없을 시
     * @throws TicketOwnershipException 사용자가 티켓의 소유자가 아닐 시
     */
    @Transactional
    fun swipeTicket(
        id: Long,
        userId: Long,
    ): TicketResponse {
        val ticket = findById(id)

        if (ticket.swipeAndCheckCompletion(userId)) {
            completeTicket(ticket)
        }

        return ticket.toResponse()
    }

    /**
     * 사용자의 미완료된 티켓 조회
     * @param userId 사용자의 Id
     * @return 조회된 사용자의 티켓 리스트
     */
    @Transactional(readOnly = true)
    fun findIncompleteTickets(userId: Long): List<TicketResponse> = repo.findIncompleteTicketsOfUser(userId).map { it.toResponse() }

    /**
     * 티켓 스와이프 취소
     * @param id 티켓의 Id
     * @param userId 티켓의 소유자 Id
     * @return 스와이프 취소 처리된 티켓 응답
     * @throws TicketNotFoundException 티켓이 존재하지 않을 때
     * @throws TicketOwnershipException 사용자가 티켓의 소유자가 아닐 시
     */
    fun cancelSwipeTicket(
        id: Long,
        userId: Long,
    ): TicketResponse {
        val ticket = findById(id)

        ticket.cancelSwipeTicket(userId)

        return ticket.toResponse()
    }

    /**
     * 티켓 삭제 메소드
     * @param id 티켓 id
     * @param userId 사용자 id
     * @throws TicketNotFoundException 티켓이 존재하지 않을 시
     * @throws TicketOwnershipException 사용자가 티켓의 소유자가 아닐시
     */
    @Transactional
    fun deleteTicket(
        id: Long,
        userId: Long,
    ) {
        val ticket = findById(id)

        ticket.checkOwnership(userId)
        repo.delete(ticket)
    }

    /**
     * 사용자의 티켓 삭제 메소드
     * @param userId 사용자 id
     */
    @Transactional
    fun deleteByUserId(userId: Long) = repo.deleteByUserId(userId)

    /**
     * 티켓 완료 메소드, kafka를 통해 이벤트를 전송한다.
     * @param ticket 완료된 티켓
     */
    private fun completeTicket(ticket: Ticket) = completionEventService.publishEvent(TicketCompletedEvent(ticket))

    /**
     * 티켓 조회 메소드
     * @param id 티켓의 id
     * @throws TicketNotFoundException id인 티켓이 존재하지 않을 시
     */
    private fun findById(id: Long) = repo.findById(id).orElseThrow { TicketNotFoundException(id) }
}

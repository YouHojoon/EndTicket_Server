package ac.kr.smu.endTicket.ticket

import ac.kr.smu.endTicket.test.mockAny
import ac.kr.smu.endTicket.ticket.domain.exception.TicketNotFoundException
import ac.kr.smu.endTicket.ticket.domain.exception.TicketOwnershipException
import ac.kr.smu.endTicket.ticket.domain.model.Ticket
import ac.kr.smu.endTicket.ticket.domain.repository.TicketRepository
import ac.kr.smu.endTicket.ticket.service.TicketCompletionEventService
import ac.kr.smu.endTicket.ticket.service.TicketService
import ac.kr.smu.endTicket.ticket.ui.request.TicketRequest
import ac.kr.smu.endTicket.ticket.ui.response.TicketResponse
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations
import org.mockito.junit.jupiter.MockitoExtension
import java.util.*
import kotlin.test.assertEquals

@ExtendWith(MockitoExtension::class)
class TicketServiceTest (
    @Mock
    private val repo: TicketRepository,
    @Mock
    private val eventService: TicketCompletionEventService,


) {
    @InjectMocks
    private lateinit var service: TicketService
    @BeforeEach
    fun init(){
        MockitoAnnotations.openMocks(this)
    }

    @Test
    @DisplayName("티켓 생성 테스트")
    fun given_ticketRequest_when_createTicket_then_returnCreatedTicket(){
        val ticket = Ticket.from(TICKET_REQUEST, USER_ID)
        Mockito.`when`(repo.save(ticket))
            .thenReturn(ticket)

        assertEquals(TicketResponse.from(ticket), service.createTicket(TICKET_REQUEST, USER_ID))
        Mockito.verify(repo, Mockito.times(1)).countIncompleteTicketsOfUser(USER_ID)
    }
    @Test
    @DisplayName("티켓 개수 제한 이상으로 생성 테스트")
    fun given_userHasReachedTicketLimit_when_createTicket_then_throwIllegalStateException(){
        Mockito.`when`(repo.countIncompleteTicketsOfUser(USER_ID))
            .thenReturn(5)

        assertThrows<IllegalStateException> { service.createTicket(TICKET_REQUEST, USER_ID) }
    }
    @Test
    @DisplayName("티켓 수정 테스트")
    fun given_ticketRequest_when_updateTicket_then_returnUpdatedTicket(){
        val ticket = Ticket.from(TICKET_REQUEST, USER_ID)

            Mockito.`when`(repo.findById(Mockito.anyLong()))
            .thenReturn(Optional.of(ticket))

        assertEquals(service.updateTicket(UPDATE_REQUEST, ticket.id, USER_ID) , TicketResponse.from(ticket))
    }
    @Test
    @DisplayName("존재하지 않는 티켓 수정 테스트")
    fun given_notExistTicket_when_updateTicket_then_throwTicketNotFoundException(){
        Mockito.`when`(repo.findById(Mockito.anyLong()))
            .thenReturn(Optional.empty())
        assertThrows<TicketNotFoundException> {  service.updateTicket(UPDATE_REQUEST, 1L, USER_ID)}
    }
    @Test
    @DisplayName("티켓 수정 후 티켓 완료 테스트")
    fun given_completeTicketAfterUpdate_when_updateTicket_then_runCompleteTicket(){
        val ticket = Ticket.from(
            TicketRequest(
                "b",
                "t",
                Ticket.Color.RED2,
                Ticket.Type.HEALTH,
                Ticket.MaxSwipeCount.TEN
            ),
            USER_ID
        )

        repeat(Ticket.MaxSwipeCount.FIVE.value){
            ticket.swipeAndCheckCompletion(USER_ID)
        }

        Mockito.`when`(repo.findById(ticket.id))
            .thenReturn(Optional.of(ticket))

        val updatedTicket = service.updateTicket(TICKET_REQUEST, ticket.id , USER_ID)

        assertEquals(TicketResponse.from(ticket), updatedTicket)
        Mockito.verify(eventService, Mockito.times(1)).eventPublish(mockAny())
    }

    @Test
    @DisplayName("티켓의 소유자가 아닌 사용자의 수정 테스트")
    fun given_userWhoNotOwnerOfTicket_then_throwTicketOwnershipException(){
        val ticket = Ticket.from(TICKET_REQUEST, USER_ID)
        Mockito.`when`(repo.findById(ticket.id))
            .thenReturn(Optional.of(ticket))
        assertThrows<TicketOwnershipException> {  service.updateTicket(UPDATE_REQUEST, ticket.id, 2L)}
    }

    @Test
    @DisplayName("티켓 스와이프 테스트")
    fun given_id_when_swipeTicket_then_returnSwipedTicket(){
        val ticket = Ticket.from(TICKET_REQUEST, USER_ID)
        val beforeSwipeCount = ticket.swipeCount

        Mockito.`when`(repo.findById(ticket.id))
            .thenReturn(Optional.of(ticket))

        val swipedTicket = service.swipeTicket(ticket.id, ticket.userID)

        assertEquals(beforeSwipeCount + 1, swipedTicket.swipeCount)
    }
    @Test
    @DisplayName("티켓 소유자가 아닌 사용자의 스와이프 테스트")
    fun given_userWhoNotOwnerOfTicket_whenSwipeTicket_then_throwTicketOwnershipException(){
        val ticket = Ticket.from(TICKET_REQUEST, USER_ID)

        Mockito.`when`(repo.findById(ticket.id))
            .thenReturn(Optional.of(ticket))
        assertThrows<TicketOwnershipException> {  service.swipeTicket(ticket.id, 2L)}
    }
    @Test
    @DisplayName("존재하지 않는 티켓 스와이프 테스트")
    fun given_notExistTicket_when_swipeTicket_then_throwTicketNotFoundException() {
        Mockito.`when`(repo.findById(Mockito.anyLong()))
            .thenReturn(Optional.empty())

        assertThrows<TicketNotFoundException> { service.swipeTicket(1L,1L) }
    }

    @Test
    @DisplayName("스와이프 취소 테스트")
    fun given_id_when_cancelSwipe_then_returnSwipeCanceledTicket(){
        val ticket = Ticket.from(TICKET_REQUEST, USER_ID).also { it.swipeAndCheckCompletion(it.userID) }
        val beforeSwipeCount = ticket.swipeCount

        Mockito.`when`(repo.findById(ticket.id))
            .thenReturn(Optional.of(ticket))

        service.cancelSwipeTicket(ticket.id, ticket.userID)

        assertEquals(beforeSwipeCount - 1, ticket.swipeCount)
    }

    @Test
    @DisplayName("존재하지 않는 티켓 스와이프 취소 테스트")
    fun given_nonExistTicket_when_swipeTicket_then_throwTicketNotFoundException(){
        Mockito.`when`(repo.findById(Mockito.anyLong()))
            .thenReturn(Optional.empty())

        assertThrows<TicketNotFoundException> { service.cancelSwipeTicket(1L, 1L)}
    }

    @Test
    @DisplayName("소유자가 아닌 사용자 티켓 스와이프 취소 테스트")
    fun given_userWhoNotOwnerOfTicket_whenCancelSwipeTicket_then_throwTicketOwnershipException(){
        val ticket = Ticket.from(TICKET_REQUEST, USER_ID)
        ticket.swipeAndCheckCompletion(ticket.userID)

        Mockito.`when`(repo.findById(ticket.id))
            .thenReturn(Optional.of(ticket))

        assertThrows<TicketOwnershipException> {  service.cancelSwipeTicket(ticket.id, 2L)}
    }

    @Test
    @DisplayName("티켓 완료 테스트")
    fun given_ticketWhichRightBeforeCompletion_when_swipeTicket_then_runCompleteTicket(){
        val ticket = Ticket.from(TICKET_REQUEST, USER_ID)

        Mockito.`when`(repo.findById(ticket.id)).thenReturn(Optional.of(ticket))

        repeat(ticket.maxSwipeCount.value){
            service.swipeTicket(ticket.id, ticket.userID)
        }

        Mockito.verify(eventService).eventPublish(mockAny())
    }

    @Test
    @DisplayName("미완료된 티켓 조회")
    fun given_userID_when_findIncompleteTickets_then_returnIncompleteTickets(){
        val tickets = listOf(Ticket.from(TICKET_REQUEST, USER_ID))
        Mockito.`when`(repo.findIncompleteTicketsOfUser(USER_ID))
            .thenReturn(tickets)

        assertEquals(tickets.map { TicketResponse.from(it) }, service.findIncompleteTickets(USER_ID))
    }
    @Test
    @DisplayName("존재하지 않는 티켓 삭제 테스트")
    fun given_notExistTicket_when_deleteTicket_then_throwTicketNotFoundException(){
        Mockito.`when`(repo.findById(Mockito.anyLong()))
            .thenReturn(Optional.empty())

        assertThrows<TicketNotFoundException> { service.deleteTicket(1L, USER_ID)}
    }

    @Test
    @DisplayName("소유자가 아닌 사용자의 티켓 삭제 테스트")
    fun given_userWhoNotOwner_when_deleteTicket_then_throwTicketOwnershipException(){
        val ticket = Ticket.from(TICKET_REQUEST, USER_ID)

        Mockito.`when`(repo.findById(ticket.id))
            .thenReturn(Optional.of(ticket))

        assertThrows<TicketOwnershipException> {  service.deleteTicket(ticket.id, 2)}
    }
}
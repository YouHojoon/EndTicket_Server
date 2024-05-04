package ac.kr.smu.endTicket

import ac.kr.smu.endTicket.ticket.domain.exception.NotOwnerOfTicketException
import ac.kr.smu.endTicket.ticket.domain.model.Ticket
import ac.kr.smu.endTicket.ticket.domain.repository.TicketRepository
import ac.kr.smu.endTicket.ticket.service.TicketService

import ac.kr.smu.endTicket.ticket.ui.request.TicketRequest
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
import javax.swing.text.html.Option
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@ExtendWith(MockitoExtension::class)
class TicketServiceTest(
    @Mock
    private val repo: TicketRepository,
) {
    @InjectMocks
    private lateinit var service: TicketService
    private val USER_ID = 1L
    @BeforeEach
    private fun init(){
        MockitoAnnotations.openMocks(this)
    }

    @Test
    @DisplayName("티켓 생성 테스트")
    fun given_ticket_when_createTicket_then_createTicket_and_returnCreatedTicket(){
        val ticket = createTicket()
        Mockito.`when`(repo.save(Mockito.any()))
            .thenReturn(ticket)

        assertEquals(ticket, service.createTicket(ticket.toTicketRequest(), USER_ID))
    }

    @Test
    @DisplayName("티켓 수정 테스트")
    fun given_ticket_when_updateTicket_then_updateTicket_and_returnUpdatedTicket(){
        val ticket = createTicket()
        Mockito.`when`(repo.findById(Mockito.anyLong()))
            .thenReturn(Optional.of(createTicket()))
        ticket.behavior = "bb"

        assertEquals(service.updateTicket(ticket.toTicketRequest(), ticket.id, USER_ID) , ticket)
    }
    @Test
    @DisplayName("id인 티켓이 존재하지 않을 때 티켓 수정 테스트")
    fun given_notExistTicketForID_when_updateTicket_then_throwIllegalStateException(){
        val ticket = createTicket()
        Mockito.`when`(repo.findById(ticket.id))
            .thenReturn(Optional.empty())
        assertThrows<IllegalStateException> {  service.updateTicket(ticket.toTicketRequest(), ticket.id, USER_ID)}
    }
    @Test
    @DisplayName("티켓의 소유자가 아닌 사용자의 수정 테스트")
    fun given_userWhoNotOwnerOfTicket_then_throwNotOwnerOfTicketException(){
        val ticket = createTicket()
        Mockito.`when`(repo.findById(ticket.id))
            .thenReturn(Optional.of(ticket))
        assertThrows<NotOwnerOfTicketException> {  service.updateTicket(ticket.toTicketRequest(), ticket.id, 2L)}
    }


    private fun createTicket(): Ticket{
        return Ticket(
            behavior = "b",
            target = "t",
            color = Ticket.Color.BLUE1,
            type = Ticket.Type.SELF_IMPORVEMENT,
            swipeCount = Ticket.SwipeCount.FIVE,
            userID = USER_ID
            )
    }

    private fun Ticket.toTicketRequest() =
        TicketRequest(
        behavior = behavior,
        target = target,
        color = color,
        type = type,
        swipeCount = swipeCount
    )
}
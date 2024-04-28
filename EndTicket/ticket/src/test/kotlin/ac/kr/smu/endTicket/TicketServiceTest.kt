package ac.kr.smu.endTicket

import ac.kr.smu.endTicket.ticket.domain.model.Ticket
import ac.kr.smu.endTicket.ticket.domain.repository.TicketRepository
import ac.kr.smu.endTicket.ticket.service.TicketService
import ac.kr.smu.endTicket.ticket.ui.request.CreateTicketRequest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations
import org.mockito.junit.jupiter.MockitoExtension
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
    fun given_ticket_when_createTicket_then_createTicket(){
        val ticket = createTicket()
        Mockito.`when`(repo.save(Mockito.any()))
            .thenReturn(ticket)

        assertEquals(ticket, service.createTicket(
            CreateTicketRequest(
                behavior = ticket.behavior,
                target = ticket.target,
                color = ticket.color,
                type = ticket.type,
                swipeCount = ticket.swipeCount
            )
            ,
            USER_ID))
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
}
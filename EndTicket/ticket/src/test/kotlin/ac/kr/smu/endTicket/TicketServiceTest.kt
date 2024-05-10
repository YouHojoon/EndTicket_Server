package ac.kr.smu.endTicket

import ac.kr.smu.endTicket.ticket.domain.exception.NotFoundTicketException
import ac.kr.smu.endTicket.ticket.domain.exception.NotOwnerOfTicketException
import ac.kr.smu.endTicket.ticket.domain.model.Ticket
import ac.kr.smu.endTicket.ticket.domain.repository.TicketRepository
import ac.kr.smu.endTicket.ticket.service.TicketService
import ac.kr.smu.endTicket.ticket.ui.request.TicketRequest
import org.junit.jupiter.api.*
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.data.redis.core.RedisTemplate
import java.util.*

import kotlin.test.assertEquals


@ExtendWith(MockitoExtension::class)
class TicketServiceTest(
    @Mock
    private val redisTemplate: RedisTemplate<String,Any>,
    @Mock
    private val repo: TicketRepository,
) {
    @InjectMocks
    private lateinit var service: TicketService
    companion object{
        private const val USER_ID = 1L
    }
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
    @DisplayName("존재하지 않는 티켓 수정 테스트")
    fun given_notExistTicket_when_updateTicket_then_throwNotFoundTicketException(){
        val ticket = createTicket()
        Mockito.`when`(repo.findById(ticket.id))
            .thenReturn(Optional.empty())
        assertThrows<NotFoundTicketException> {  service.updateTicket(ticket.toTicketRequest(), ticket.id, USER_ID)}
    }
    @Test
    @DisplayName("티켓의 소유자가 아닌 사용자의 수정 테스트")
    fun given_userWhoNotOwnerOfTicket_then_throwNotOwnerOfTicketException(){
        val ticket = createTicket()
        Mockito.`when`(repo.findById(ticket.id))
            .thenReturn(Optional.of(ticket))
        assertThrows<NotOwnerOfTicketException> {  service.updateTicket(ticket.toTicketRequest(), ticket.id, 2L)}
    }

    @Test
    @DisplayName("티켓 스와이프 테스트")
    fun given_ID_when_swipeTicket_then_plusOneSwipeCountOfTicket(){
        val ticket = createTicket()
        Mockito.`when`(repo.findById(ticket.id))
            .thenReturn(Optional.of(ticket))
        val beforeSwipeCount = ticket.swipeCount
        val swipedTicket = service.swipeTicket(ticket.id, USER_ID)

        assertEquals(beforeSwipeCount + 1, swipedTicket.swipeCount)
    }
    @Test
    @DisplayName("티켓 소유자가 아닌 사용자의 스와이프 테스트")
    fun given_userWhoNotOwnerOfTicket_whenSwipeTicket_then_throwNotOwnerOfTicket(){
        val ticket = createTicket()
        Mockito.`when`(repo.findById(ticket.id))
            .thenReturn(Optional.of(ticket))
        assertThrows<NotOwnerOfTicketException> {  service.swipeTicket(ticket.id, 2L)}
    }
    @Test
    @DisplayName("존재하지 않는 티켓 스와이프 테스트")
    fun given_notExistTicket_when_swipeTicket_then_throwNotFoundTicketException() {
        Mockito.`when`(repo.findById(Mockito.anyLong()))
            .thenReturn(Optional.empty())
        assertThrows<NotFoundTicketException> { service.swipeTicket(1L,USER_ID) }
    }

    @Test
    @DisplayName("스와이프 취소 테스트")
    fun given_ID_when_cancelSwipe_then_minusOneSwipeCountOfTicket(){
        val ticket = createTicket().also { it.swipeAndCheckCompletion(USER_ID) }
        val beforeSwipeCount = ticket.swipeCount

        Mockito.`when`(repo.findById(ticket.id))
            .thenReturn(Optional.of(ticket))

        service.cancelSwipeTicket(ticket.id, USER_ID)

        assertEquals(beforeSwipeCount - 1, ticket.swipeCount)
    }

    @Test
    @DisplayName("존재하지 않는 티켓 스와이프 취소 테스트")
    fun given_nonExistTicket_when_swipeTicket_then_throwNotFoundTicketException(){
        Mockito.`when`(repo.findById(Mockito.anyLong()))
            .thenReturn(Optional.empty())

        assertThrows<NotFoundTicketException> { service.cancelSwipeTicket(1L, USER_ID)}
    }

    @Test
    @DisplayName("소유자가 아닌 사용자 티켓 스와이프 취소 테스트")
    fun given_userWhoNotOwnerOfTicket_whenCancelSwipeTicket_then_throwNotOwnerOfTicket(){
        val ticket = createTicket()
        ticket.swipeAndCheckCompletion(USER_ID)

        Mockito.`when`(repo.findById(ticket.id))
            .thenReturn(Optional.of(ticket))

        assertThrows<NotOwnerOfTicketException> {  service.cancelSwipeTicket(ticket.id, 2L)}
    }

    private fun createTicket(): Ticket{
        return Ticket(
            behavior = "b",
            target = "t",
            color = Ticket.Color.BLUE1,
            type = Ticket.Type.SELF_IMPROVEMENT,
            maxSwipeCount = Ticket.MaxSwipeCount.FIVE,
            userID = USER_ID
            )
    }

    private fun Ticket.toTicketRequest() =
        TicketRequest(
        behavior = behavior,
        target = target,
        color = color,
        type = type,
        maxSwipeCount = maxSwipeCount
    )
}
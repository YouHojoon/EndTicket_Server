package ac.kr.smu.endTicket

import ac.kr.smu.endTicket.ticket.domain.exception.NotFoundTicketException
import ac.kr.smu.endTicket.ticket.domain.exception.NotOwnerOfTicketException
import ac.kr.smu.endTicket.ticket.domain.model.Ticket
import ac.kr.smu.endTicket.ticket.domain.repository.TicketRepository
import ac.kr.smu.endTicket.ticket.service.TicketService
import ac.kr.smu.endTicket.ticket.ui.request.TicketRequest
import org.junit.jupiter.api.*
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.core.ValueOperations
import org.springframework.scheduling.annotation.EnableScheduling
import java.util.*

import kotlin.test.assertEquals


@SpringBootTest(
    properties = [
        "schedules.save-updatedTicket-toDB.initialDelay=50",
        "schedules.save-updatedTicket-toDB.fixedDelay=100"
    ],
    classes = [TicketService::class]
)
@EnableScheduling
class TicketServiceTest @Autowired constructor(
    @MockBean
    private val ops: ValueOperations<String, Any>,
    @MockBean
    private val redisTemplate: RedisTemplate<String,Any>,
    @MockBean
    private val repo: TicketRepository,

    private val service: TicketService
) {
    companion object{
        private const val USER_ID = 1L
    }


    @BeforeEach
    fun init(){
        Mockito.`when`(redisTemplate.opsForValue())
            .thenReturn(ops)
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
    fun given_notExistTicket_when_updateTicket_then_throwIllegalStateException(){
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
    fun given_ID_when_swipeTicket_then_plus1AtSwipeCountAtTicket(){
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
    fun given_notExistTicket_when_swipeTicket_then_throwIllegalStateException() {
        Mockito.`when`(repo.findById(Mockito.anyLong()))
            .thenReturn(Optional.empty())
        assertThrows<NotFoundTicketException> { service.swipeTicket(1L,USER_ID) }
    }
    @Test
    @DisplayName("Write Back 패턴 테스트")
    fun after_fixedDelay_then_invokeSaveUpdatedTicketToDB(){
        Thread.sleep( 300)
        Mockito.verify(repo, Mockito.atLeast(2)).saveAll(Mockito.anyList())
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
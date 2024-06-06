package ac.kr.smu.endTicket.ticket

import ac.kr.smu.endTicket.common.redis.annotation.EnableAutoRedisConfig
import ac.kr.smu.endTicket.ticket.domain.exception.NotFoundTicketException
import ac.kr.smu.endTicket.ticket.domain.exception.NotOwnerOfTicketException
import ac.kr.smu.endTicket.ticket.domain.model.Ticket
import ac.kr.smu.endTicket.ticket.domain.repository.TicketRepository
import ac.kr.smu.endTicket.ticket.service.TicketCompletionEventService
import ac.kr.smu.endTicket.ticket.service.TicketService
import ac.kr.smu.endTicket.ticket.ui.request.TicketRequest
import ac.kr.smu.endTicket.ticket.ui.response.TicketResponse
import jakarta.persistence.EntityManager
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration
import org.springframework.boot.autoconfigure.transaction.TransactionAutoConfiguration
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.core.ValueOperations
import java.util.*
import kotlin.test.assertEquals


@SpringBootTest(
    properties = [
        "eureka.client.enabled=false"
    ],
    classes = [
        TicketService::class,
        DataSourceAutoConfiguration::class,
        HibernateJpaAutoConfiguration::class,
        TransactionAutoConfiguration::class,
        RedisAutoConfiguration::class
    ]
)
@EntityScan("ac.kr.smu.endTicket.ticket.domain.model")
@EnableAutoRedisConfig
class TicketServiceTest @Autowired constructor(
    @MockBean
    private val ops: ValueOperations<String,Any>,
    @MockBean
    private val redisTemplate: RedisTemplate<String, Any>,
    @MockBean
    private val repo: TicketRepository,
    @MockBean
    private val eventService: TicketCompletionEventService,
    @MockBean
    private val em: EntityManager,
    private val service: TicketService,
) {
    private companion object{
        private const val REDIS_KEY_PREFIX = "ticket::"
    }
    @BeforeEach
    fun init(){
        Mockito.`when`(redisTemplate.opsForValue()).thenReturn(ops)
    }

    @Test
    @DisplayName("티켓 생성 테스트")
    fun given_ticketRequest_when_createTicket_then_createTicket_and_returnCreatedTicket(){
        val ticket = Ticket.from(TICKET_REQUEST, USER_ID)
        Mockito.`when`(repo.save(ticket))
            .thenReturn(ticket)

        assertEquals(TicketResponse.from(ticket), service.createTicket(TICKET_REQUEST, USER_ID))
        Mockito.verify(repo, Mockito.times(1)).countByUserID(USER_ID)
    }
    @Test
    @DisplayName("티켓 개수 제한 이상으로 생성 테스트")
    fun given_userHasReachedTicketLimit_when_createTicket_then_throwIllegalStateException(){
        Mockito.`when`(repo.countByUserID(USER_ID))
            .thenReturn(5)

        assertThrows<IllegalStateException> { service.createTicket(TICKET_REQUEST, USER_ID) }
    }
    @Test
    @DisplayName("티켓 수정 테스트")
    fun given_ticketRequest_when_updateTicket_then_updateTicket_and_returnUpdatedTicket(){
        val ticket = Ticket.from(TICKET_REQUEST, USER_ID)

            Mockito.`when`(repo.findById(Mockito.anyLong()))
            .thenReturn(Optional.of(ticket))


        assertEquals(service.updateTicket(UPDATE_REQUEST, ticket.id, USER_ID) , TicketResponse.from(ticket))
    }
    @Test
    @DisplayName("존재하지 않는 티켓 수정 테스트")
    fun given_notExistTicket_when_updateTicket_then_throwNotFoundTicketException(){
        Mockito.`when`(repo.findById(Mockito.anyLong()))
            .thenReturn(Optional.empty())
        assertThrows<NotFoundTicketException> {  service.updateTicket(UPDATE_REQUEST, 1L, USER_ID)}
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

        Mockito.`when`(redisTemplate.delete("$REDIS_KEY_PREFIX${ticket.id}")).thenReturn(true)
        Mockito.`when`(repo.findById(ticket.id))
            .thenReturn(Optional.of(ticket))

        val updatedTicket = service.updateTicket(TICKET_REQUEST, ticket.id , USER_ID)
        assertEquals(TicketResponse.from(ticket), updatedTicket)
        verifyCompleteTicket(ticket)
    }

    @Test
    @DisplayName("티켓의 소유자가 아닌 사용자의 수정 테스트")
    fun given_userWhoNotOwnerOfTicket_then_throwNotOwnerOfTicketException(){
        val ticket = Ticket.from(TICKET_REQUEST, USER_ID)
        Mockito.`when`(repo.findById(ticket.id))
            .thenReturn(Optional.of(ticket))
        assertThrows<NotOwnerOfTicketException> {  service.updateTicket(UPDATE_REQUEST, ticket.id, 2L)}
    }

    @Test
    @DisplayName("티켓 스와이프 테스트")
    fun given_ID_when_swipeTicket_then_returnSwipedTicket(){
        val ticket = Ticket.from(TICKET_REQUEST, USER_ID)
        val beforeSwipeCount = ticket.swipeCount

        Mockito.`when`(repo.findById(ticket.id))
            .thenReturn(Optional.of(ticket))

        val swipedTicket = service.swipeTicket(ticket.id, ticket.userID)

        assertEquals(beforeSwipeCount + 1, swipedTicket.swipeCount)
    }
    @Test
    @DisplayName("티켓 소유자가 아닌 사용자의 스와이프 테스트")
    fun given_userWhoNotOwnerOfTicket_whenSwipeTicket_then_throwNotOwnerOfTicketException(){
        val ticket = Ticket.from(TICKET_REQUEST, USER_ID)

        Mockito.`when`(repo.findById(ticket.id))
            .thenReturn(Optional.of(ticket))
        assertThrows<NotOwnerOfTicketException> {  service.swipeTicket(ticket.id, 2L)}
    }
    @Test
    @DisplayName("존재하지 않는 티켓 스와이프 테스트")
    fun given_notExistTicket_when_swipeTicket_then_throwNotFoundTicketException() {
        Mockito.`when`(repo.findById(Mockito.anyLong()))
            .thenReturn(Optional.empty())

        assertThrows<NotFoundTicketException> { service.swipeTicket(1L,1L) }
    }

    @Test
    @DisplayName("스와이프 취소 테스트")
    fun given_ID_when_cancelSwipe_then_returnSwipeCanceledTicket(){
        val ticket = Ticket.from(TICKET_REQUEST, USER_ID).also { it.swipeAndCheckCompletion(it.userID) }
        val beforeSwipeCount = ticket.swipeCount

        Mockito.`when`(repo.findById(ticket.id))
            .thenReturn(Optional.of(ticket))

        service.cancelSwipeTicket(ticket.id, ticket.userID)

        assertEquals(beforeSwipeCount - 1, ticket.swipeCount)
    }

    @Test
    @DisplayName("존재하지 않는 티켓 스와이프 취소 테스트")
    fun given_nonExistTicket_when_swipeTicket_then_throwNotFoundTicketException(){
        Mockito.`when`(repo.findById(Mockito.anyLong()))
            .thenReturn(Optional.empty())

        assertThrows<NotFoundTicketException> { service.cancelSwipeTicket(1L, 1L)}
    }

    @Test
    @DisplayName("소유자가 아닌 사용자 티켓 스와이프 취소 테스트")
    fun given_userWhoNotOwnerOfTicket_whenCancelSwipeTicket_then_throwNotOwnerOfTicket(){
        val ticket = Ticket.from(TICKET_REQUEST, USER_ID)
        ticket.swipeAndCheckCompletion(ticket.userID)

        Mockito.`when`(repo.findById(ticket.id))
            .thenReturn(Optional.of(ticket))

        assertThrows<NotOwnerOfTicketException> {  service.cancelSwipeTicket(ticket.id, 2L)}
    }

    @Test
    @DisplayName("티켓 완료 테스트")
    fun given_ticketWhichRightBeforeCompletion_when_swipeTicket_then_runCompleteTicket(){
        val ticket = Ticket.from(TICKET_REQUEST, USER_ID)

        Mockito.`when`(repo.findById(ticket.id)).thenReturn(Optional.of(ticket))
        Mockito.`when`(redisTemplate.delete("$REDIS_KEY_PREFIX${ticket.id}"))
            .thenReturn(true)
        Mockito.`when`(em.merge(ticket)).thenReturn(ticket)

        repeat(ticket.maxSwipeCount.value){
            service.swipeTicket(ticket.id, ticket.userID)
        }

        verifyCompleteTicket(ticket)
    }

    @Test
    @DisplayName("미완료된 티켓 조회")
    fun given_userID_when_findIncompleteTickets_then_returnIncompleteTickets(){
        val tickets = listOf(Ticket.from(TICKET_REQUEST, USER_ID))
        Mockito.`when`(repo.findIncompleteTicketsOfUser(USER_ID))
            .thenReturn(tickets)

        assertEquals(tickets.map { TicketResponse.from(it) }, service.findIncompleteTicket(USER_ID))
    }

    @Suppress("UNCHECKED_CAST")
    private fun <T> any(): T{
        Mockito.any<T>()
        return null as T
    }

    private fun verifyCompleteTicket(ticket: Ticket){
        Mockito.verify(redisTemplate, Mockito.times(1)).delete("$REDIS_KEY_PREFIX${ticket.id}")
        Mockito.verify(eventService, Mockito.times(1)).eventPublish(any())
    }
}
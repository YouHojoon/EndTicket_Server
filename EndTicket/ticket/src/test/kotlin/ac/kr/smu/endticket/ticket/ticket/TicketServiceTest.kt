package ac.kr.smu.endticket.ticket.ticket

import ac.kr.smu.endticket.common.test.mockAny
import ac.kr.smu.endticket.common.web.enum.Color
import ac.kr.smu.endticket.common.web.enum.TicketType
import ac.kr.smu.endticket.ticket.domain.model.Ticket
import ac.kr.smu.endticket.ticket.domain.repository.TicketRepository
import ac.kr.smu.endticket.ticket.service.TicketCompletedEventService
import ac.kr.smu.endticket.ticket.service.TicketService
import ac.kr.smu.endticket.ticket.ui.request.TicketRequest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations
import org.mockito.junit.jupiter.MockitoExtension
import java.util.*
import kotlin.reflect.KClass
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

@ExtendWith(MockitoExtension::class)
class TicketServiceTest (
    @Mock
    private val repo: TicketRepository,
    @Mock
    private val eventService: TicketCompletedEventService,


    ) {
    @InjectMocks
    private lateinit var service: TicketService
    @BeforeEach
    fun init(){
        MockitoAnnotations.openMocks(this)
    }

    @ParameterizedTest
    @DisplayName("티켓 생성 테스트")
    @MethodSource("${TicketTestParameters.PATH}#provideTicket")
    fun given_ticketRequest_when_createTicket_then_returnCreatedTicket(ticket: Ticket){
        Mockito.`when`(repo.save(ticket))
            .thenReturn(ticket)

        assertEquals(ticket.toResponse(), service.createTicket(
            TicketTestParameters.TICKET_REQUEST,
            TicketTestParameters.USER_ID
        ))
        Mockito.verify(repo, Mockito.times(1)).countIncompleteTicketsOfUser(TicketTestParameters.USER_ID)
    }
    @Test
    @DisplayName("티켓 개수 제한 이상으로 생성 테스트")
    fun given_userHasReachedTicketLimit_when_createTicket_then_throwIllegalStateException(){
        Mockito.`when`(repo.countIncompleteTicketsOfUser(TicketTestParameters.USER_ID))
            .thenReturn(5)

        assertThrows<IllegalStateException> { service.createTicket(
            TicketTestParameters.TICKET_REQUEST,
            TicketTestParameters.USER_ID
        ) }
    }
    @ParameterizedTest
    @DisplayName("티켓 수정 테스트")
    @MethodSource("${TicketTestParameters.PATH}#provideTicket")
    fun given_ticketRequest_when_updateTicket_then_returnUpdatedTicket(ticket: Ticket){
            Mockito.`when`(repo.findById(Mockito.anyLong()))
            .thenReturn(Optional.of(ticket))

        assertEquals(service.updateTicket(TicketTestParameters.UPDATE_REQUEST, ticket.id, TicketTestParameters.USER_ID) , ticket.toResponse())
    }
    @ParameterizedTest
    @DisplayName("비정상적인 티켓 수정 테스트")
    @MethodSource("${TicketTestParameters.PATH}#provideInvalidTicket")
    fun given_invalidId_when_updateTicket_then_throwExpectedException(
        ticket: Ticket?,
        userId: Long,
        exception: KClass<out Throwable>
    ){
        val id = ticket?.id ?: 1L
        Mockito.`when`(repo.findById(id))
            .thenReturn(Optional.ofNullable(ticket))
        assertFailsWith(exception) {  service.updateTicket(TicketTestParameters.UPDATE_REQUEST, id, userId)}
    }

    @Test
    @DisplayName("티켓 수정 후 티켓 완료 테스트")
    fun given_completeTicketAfterUpdate_when_updateTicket_then_runCompleteTicket(){
        val ticket = Ticket.from(
            TicketRequest(
                "b",
                "t",
                Color.RED2,
                TicketType.HEALTH,
                Ticket.MaxSwipeCount.TEN
            ),
            TicketTestParameters.USER_ID
        )

        repeat(Ticket.MaxSwipeCount.FIVE.value){
            ticket.swipeAndCheckCompletion(TicketTestParameters.USER_ID)
        }

        Mockito.`when`(repo.findById(ticket.id))
            .thenReturn(Optional.of(ticket))

        val updatedTicket = service.updateTicket(
            TicketTestParameters.TICKET_REQUEST, ticket.id ,
            TicketTestParameters.USER_ID
        )

        assertEquals(ticket.toResponse(), updatedTicket)
        Mockito.verify(eventService, Mockito.times(1)).publishEvent(mockAny())
    }

    @ParameterizedTest
    @DisplayName("티켓 스와이프 테스트")
    @MethodSource("${TicketTestParameters.PATH}#provideTicket")
    fun given_id_when_swipeTicket_then_returnSwipedTicket(ticket: Ticket){
        val beforeSwipeCount = ticket.swipeCount

        Mockito.`when`(repo.findById(ticket.id))
            .thenReturn(Optional.of(ticket))

        val swipedTicket = service.swipeTicket(ticket.id, ticket.userId)

        assertEquals(beforeSwipeCount + 1, swipedTicket.swipeCount)
    }
    @ParameterizedTest
    @DisplayName("티켓 소유자가 아닌 사용자의 스와이프 테스트")
    @MethodSource("${TicketTestParameters.PATH}#provideInvalidTicket")
    fun given_invalidTicket_whenSwipeTicket_then_throwExpectedException(
        ticket: Ticket?,
        userId: Long,
        exception: KClass<out Throwable>
    ){
        val id = ticket?.id ?: 1L

        Mockito.`when`(repo.findById(id))
            .thenReturn(Optional.ofNullable(ticket))

        assertFailsWith (exception){  service.swipeTicket(id, userId)}
    }
    @ParameterizedTest
    @DisplayName("스와이프 취소 테스트")
    @MethodSource("${TicketTestParameters.PATH}#provideTicket")
    fun given_id_when_cancelSwipe_then_returnSwipeCanceledTicket(ticket: Ticket){
        ticket.swipeAndCheckCompletion(ticket.userId)
        val beforeSwipeCount = ticket.swipeCount

        Mockito.`when`(repo.findById(ticket.id))
            .thenReturn(Optional.of(ticket))

        service.cancelSwipeTicket(ticket.id, ticket.userId)

        assertEquals(beforeSwipeCount - 1, ticket.swipeCount)
    }

    @ParameterizedTest
    @DisplayName("비정상적인 티켓 스와이프 취소 테스트")
    @MethodSource("${TicketTestParameters.PATH}#provideInvalidTicket")
    fun given_invalidTicket_when_swipeTicket_then_throwExpectedException(
        ticket: Ticket?,
        userId: Long,
        exception: KClass<out Throwable>
    ){
        val id = ticket?.id ?: 1L
        Mockito.`when`(repo.findById(id))
            .thenReturn(Optional.ofNullable(ticket))

        assertFailsWith(exception) { service.cancelSwipeTicket(id, userId)}
    }
    @ParameterizedTest
    @DisplayName("티켓 완료 테스트")
    @MethodSource("${TicketTestParameters.PATH}#provideTicket")
    fun given_ticketWhichRightBeforeCompletion_when_swipeTicket_then_runCompleteTicket(ticket: Ticket){
        Mockito.`when`(repo.findById(ticket.id)).thenReturn(Optional.of(ticket))

        repeat(TicketTestParameters.TICKET_REQUEST.maxSwipeCount.value){
            service.swipeTicket(ticket.id, ticket.userId)
        }

        Mockito.verify(eventService).publishEvent(mockAny())
    }

    @ParameterizedTest
    @DisplayName("미완료된 티켓 조회")
    @MethodSource("${TicketTestParameters.PATH}#provideTicket")
    fun given_userId_when_findIncompleteTickets_then_returnIncompleteTickets(ticket: Ticket){
        val tickets = listOf(ticket)

        Mockito.`when`(repo.findIncompleteTicketsOfUser(TicketTestParameters.USER_ID))
            .thenReturn(tickets)

        assertEquals(tickets.map { it.toResponse() }, service.findIncompleteTickets(TicketTestParameters.USER_ID))
    }
    @ParameterizedTest
    @DisplayName("비정상적인 티켓 삭제 테스트")
    @MethodSource("${TicketTestParameters.PATH}#provideInvalidTicket")
    fun given_notExistTicket_when_deleteTicket_then_throwTicketNotFoundException(
        ticket: Ticket?,
        userId: Long,
        exception: KClass<out Throwable>
    ){
        val id = ticket?.id ?: 1L
        Mockito.`when`(repo.findById(id))
            .thenReturn(Optional.ofNullable(ticket))

        assertFailsWith(exception) { service.deleteTicket(id, userId)}
    }

    @Test
    @DisplayName("사용자의 티켓 삭제 테스트")
    fun given_userId_when_deleteByUserId_then_deleteTicketOfUser(){
        service.deleteByUserId(TicketTestParameters.USER_ID)

        Mockito.verify(repo).deleteByUserId(TicketTestParameters.USER_ID)
    }
}
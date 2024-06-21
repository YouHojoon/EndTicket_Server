package ac.kr.smu.endticket.ticket

import ac.kr.smu.endticket.common.web.aop.BindExceptionAdvice
import ac.kr.smu.endticket.common.web.test.expectBindException
import ac.kr.smu.endticket.common.web.test.expectExceptionResponse
import ac.kr.smu.endticket.ticket.domain.model.Ticket
import ac.kr.smu.endticket.ticket.service.TicketService
import ac.kr.smu.endticket.ticket.ui.controller.TicketController
import ac.kr.smu.endticket.ticket.ui.request.TicketRequest
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.test.web.servlet.setup.MockMvcBuilders

@SpringBootTest(
    properties = [
        "eureka.client.enabled=false"
    ],
    classes = [
        TicketController::class,
        JacksonAutoConfiguration::class
    ]
)
class TicketControllerTest @Autowired constructor(
    @MockBean
    private val service: TicketService,
    controller: TicketController
) {

    private val mvc: MockMvc =
        MockMvcBuilders.standaloneSetup(controller)
            .setControllerAdvice(BindExceptionAdvice())
            .build()

    @Test
    @DisplayName("티켓 생성 테스트")
    fun given_ticketRequest_when_createTicket_then_responseCreatedTicket(){
        val ticket = Ticket.from(TicketTestParameters.TICKET_REQUEST, TicketTestParameters.USER_ID)

        Mockito
            .`when`(service.createTicket(TicketTestParameters.TICKET_REQUEST, TicketTestParameters.USER_ID))
            .thenReturn(ticket.toResponse())

        mvc.createTicket(TicketTestParameters.TICKET_REQUEST)
            .andExpect(MockMvcResultMatchers.status().isCreated)
            .andExpect(MockMvcResultMatchers.content().string(
                ObjectMapper().writeValueAsString(
                    ticket.toResponse()
                )
            ))
    }

    @ParameterizedTest
    @DisplayName("비정상적인 티켓 생성 요청 테스트")
    @MethodSource("${TicketTestParameters.PATH}#provideInvalidRequest")
    fun given_invalidTicketRequest_when_createTicket_then_responseBindException(request: TicketRequest){
        mvc
            .createTicket(request)
            .expectBindException()
    }

    @Test
    @DisplayName("티캣 개수 제한 이상으로 생성 테스트")
    fun given_userHasReachedTicketLimit_when_createTicket_then_responseExceptionResponseWithStatus409(){
        Mockito
            .`when`(service.createTicket(TicketTestParameters.TICKET_REQUEST, TicketTestParameters.USER_ID))
            .thenThrow(IllegalStateException("티켓 개수 제한 이상으로 생성할 수 없습니다."))

        mvc.createTicket(TicketTestParameters.TICKET_REQUEST)
            .andExpect(MockMvcResultMatchers.status().isConflict)
            .expectExceptionResponse()
    }

    @ParameterizedTest
    @DisplayName("티켓 수정 테스트")
    @MethodSource("${TicketTestParameters.PATH}#provideTicket")
    fun given_ticketRequest_when_updateTicket_then_responseUpdatedTicket(ticket: Ticket){
        Mockito.`when`(service.updateTicket(TicketTestParameters.UPDATE_REQUEST, ticket.id, TicketTestParameters.USER_ID))
            .thenReturn(ticket.also { it.updateAndCheckCompletion(TicketTestParameters.UPDATE_REQUEST, TicketTestParameters.USER_ID) }.toResponse())

        mvc.updateTicket(TicketTestParameters.UPDATE_REQUEST, ticket.id)
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.content().string(
                ObjectMapper().writeValueAsString(
                    ticket.toResponse()
                )
            ))
    }

    @ParameterizedTest
    @DisplayName("비정상적인 티켓 수정 요청 테스트")
    @MethodSource("${TicketTestParameters.PATH}#provideInvalidRequest")
    fun given_invalidTicketRequest_when_updateTicket_then_responseBindException(request: TicketRequest){
        mvc.updateTicket(request, TicketTestParameters.USER_ID).expectBindException()
    }

    @ParameterizedTest
    @DisplayName("비정상적 티켓 수정 테스트")
    @MethodSource("${TicketTestParameters.PATH}#provideInvalidId")
    fun given_invalidId_when_updateTicket_then_responseExceptionResponseWithExpectedStatus(
        id: Long,
        userId: Long,
        exception: Throwable,
        status: Int
    ){
        Mockito.`when`(service.updateTicket(TicketTestParameters.UPDATE_REQUEST, id, userId))
            .thenThrow(exception)

        mvc.updateTicket(TicketTestParameters.UPDATE_REQUEST, id, userId)
            .andExpect(MockMvcResultMatchers.status().`is`(status))
            .expectExceptionResponse()
    }

    @Test
    @DisplayName("티켓 스와이프 테스트")
    fun given_Id_when_swipeTicket_then_responseSwipedTicket(){
        val ticket = Ticket.from(TicketTestParameters.TICKET_REQUEST, TicketTestParameters.USER_ID)
        val beforeSwipeCount = ticket.swipeCount

        Mockito.`when`(service.swipeTicket(ticket.id, TicketTestParameters.USER_ID))
            .thenReturn(
               ticket.also { it.swipeAndCheckCompletion(TicketTestParameters.USER_ID)}.toResponse()
            )

        mvc.swipeTicket(ticket.id)
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.jsonPath("swipeCount").value(beforeSwipeCount + 1))
    }

    @ParameterizedTest
    @DisplayName("비정상적인 티켓 스와이프 테스트")
    @MethodSource("${TicketTestParameters.PATH}#provideInvalidId")
    fun given_invalidId_when_swipeTicket_then_responseExceptionResponseWithExpectedStatus(
        id: Long,
        userId: Long,
        exception: Throwable,
        status: Int
    ){

        Mockito.`when`(service.swipeTicket(id, userId))
            .thenThrow(exception)

        mvc.swipeTicket(id, userId)
            .andExpect(MockMvcResultMatchers.status().`is`(status))
            .expectExceptionResponse()
    }

    @ParameterizedTest
    @DisplayName("티켓 스와이프 취소 테스트")
    @MethodSource("${TicketTestParameters.PATH}#provideTicket")
    fun given_Id_when_cancelSwipeTicket_then_responseSwipeCanceledTicket(ticket: Ticket){
        ticket.swipeAndCheckCompletion(TicketTestParameters.USER_ID)
        val beforeSwipeCount = ticket.swipeCount

        Mockito.`when`(service.cancelSwipeTicket(ticket.id, TicketTestParameters.USER_ID))
            .thenReturn(
                ticket
                    .also { it.cancelSwipeTicket(TicketTestParameters.USER_ID) }
                    .toResponse()
            )

        mvc.cancelSwipeTicket(ticket.id)
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.jsonPath("swipeCount").value(beforeSwipeCount - 1))
    }

    @ParameterizedTest
    @DisplayName("비정상적 티켓 스와이프 취소 테스트")
    @MethodSource("${TicketTestParameters.PATH}#provideInvalidId")
    fun given_invalidId_when_cancelSwipeTicket_then_responseExceptionResponseWithExpectedStatus(
        id: Long,
        userId: Long,
        exception: Throwable,
        status: Int
    ){
        Mockito.`when`(service.cancelSwipeTicket(id, userId))
            .thenThrow(exception)

        mvc.cancelSwipeTicket(id,userId)
            .andExpect(MockMvcResultMatchers.status().`is`(status))
            .expectExceptionResponse()
    }

    @Test
    @DisplayName("미완료된 티켓 조회")
    fun given_userId_when_findIncompleteTickets_then_responseIncompleteTickets(){
        val tickets = listOf(
            Ticket.from(TicketTestParameters.TICKET_REQUEST, TicketTestParameters.USER_ID).toResponse()
        )
        Mockito.`when`(service.findIncompleteTickets(TicketTestParameters.USER_ID))
            .thenReturn(tickets)

        mvc.findTickets()
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.jsonPath("tickets").isArray)
            .andExpect(MockMvcResultMatchers.content().string(ObjectMapper().writeValueAsString(mapOf("tickets" to service.findIncompleteTickets(
                TicketTestParameters.USER_ID
            )))))
    }

    @Test
    @DisplayName("티켓 삭제 테스트")
    fun given_id_when_deleteTicket_then_expectStatus204(){
        mvc.deleteTicket(1L)
            .andExpect(MockMvcResultMatchers.status().isNoContent)

        Mockito.verify(service, Mockito.times(1)).deleteTicket(Mockito.anyLong(), Mockito.anyLong())
    }

    @ParameterizedTest
    @DisplayName("존재하지 않는 티켓 삭제 테스트")
    @MethodSource("${TicketTestParameters.PATH}#provideInvalidId")
    fun given_notExistTicket_when_deleteTicket_then_responseExceptionResponseWithExpectedStatus(
        id: Long,
        userId: Long,
        exception: Throwable,
        status: Int
    ){
        Mockito.`when`(
            service.deleteTicket(id, userId)
        ).thenThrow(exception)

        mvc.deleteTicket(id, userId)
            .andExpect(MockMvcResultMatchers.status().`is`(status))
            .expectExceptionResponse()
    }
}
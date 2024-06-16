package ac.kr.smu.endTicket.ticket

import ac.kr.smu.endTicket.common.web.aop.BindExceptionAdvice
import ac.kr.smu.endTicket.common.web.test.expectBindingException
import ac.kr.smu.endTicket.common.web.test.expectExceptionResponse
import ac.kr.smu.endTicket.ticket.domain.exception.TicketNotFoundException
import ac.kr.smu.endTicket.ticket.domain.exception.TicketOwnershipException
import ac.kr.smu.endTicket.ticket.domain.model.Ticket
import ac.kr.smu.endTicket.ticket.service.TicketService
import ac.kr.smu.endTicket.ticket.ui.controller.TicketController
import ac.kr.smu.endTicket.ticket.ui.request.TicketRequest
import ac.kr.smu.endTicket.ticket.ui.response.TicketResponse
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
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
    fun given_ticketRequest_when_createTicket_then_expectStatusCode204_and_responseCreatedTicket(){
        val ticket = Ticket.from(TICKET_REQUEST, USER_ID)

        Mockito
            .`when`(service.createTicket(TICKET_REQUEST, USER_ID))
            .thenReturn(TicketResponse.from(ticket))

        mvc.createTicket(TICKET_REQUEST)
            .andExpect(MockMvcResultMatchers.status().isCreated)
            .andExpect(MockMvcResultMatchers.content().string(
                ObjectMapper().writeValueAsString(
                    TicketResponse.from(ticket)
                )
            ))
    }

    @Test
    @DisplayName("비정상적인 티켓 생성 테스트")
    fun given_invalidTicketRequest_when_createTicket_then_expectStatusCode400_and_responseExceptionResponse(){
        val request = TicketRequest(
            behavior = "",
            target = "",
            color = Ticket.Color.RED1,
            type = Ticket.Type.HEALTH,
            maxSwipeCount = Ticket.MaxSwipeCount.FIVE
        )

        mvc
            .createTicket(request)
            .expectBindingException()
    }

    @Test
    @DisplayName("티캣 개수 제한 이상으로 생성 테스트")
    fun given_userHasReachedTicketLimit_when_createTicket_then_expectStatusCode409_and_responseExceptionResponse(){
        Mockito
            .`when`(service.createTicket(TICKET_REQUEST, USER_ID))
            .thenThrow(IllegalStateException("티켓 개수 제한 이상으로 생성할 수 없습니다."))

        mvc.createTicket(TICKET_REQUEST)
            .andExpect(MockMvcResultMatchers.status().isConflict)
            .expectExceptionResponse()
    }

    @Test
    @DisplayName("티켓 수정 테스트")
    fun given_ticketRequest_when_updateTicket_then_expectStatusCode200_and_responseUpdatedTicket(){
        val ticket = Ticket.from(TICKET_REQUEST, USER_ID)

        Mockito.`when`(service.updateTicket(UPDATE_REQUEST, ticket.id, USER_ID)).thenReturn(TicketResponse.from(ticket))

        mvc.updateTicket(UPDATE_REQUEST, ticket.id)
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.content().string(
                ObjectMapper().writeValueAsString(
                    TicketResponse.from(ticket)
                )
            ))
    }

    @Test
    @DisplayName("비정상적인 티켓 수정 요청 테스트")
    fun given_invalidTicketRequest_when_updateTicket_then_expectStatusCode400(){
        val ticket = Ticket.from(TICKET_REQUEST, USER_ID)
        val updateRequest = TicketRequest(
            "",
            ticket.target,
            ticket.color,
            ticket.type,
            ticket.maxSwipeCount
        )

        Mockito.`when`(service.updateTicket(updateRequest, ticket.id, USER_ID)).thenReturn(TicketResponse.from(ticket))

        mvc.updateTicket(updateRequest,ticket.id).expectBindingException()
    }

    @Test
    @DisplayName("존재하지 않는 티켓 수정 요청 테스트")
    fun given_notExistTicket_when_updateTicket_then_expectStatusCode404(){
        Mockito.`when`(service.updateTicket(UPDATE_REQUEST, 1L, USER_ID))
            .thenThrow(TicketNotFoundException(1L))

        mvc.updateTicket(UPDATE_REQUEST, 1L)
            .andExpect(MockMvcResultMatchers.status().isNotFound)
            .expectExceptionResponse()
    }

    @Test
    @DisplayName("티켓의 소유자가 아닌 사용자의 티켓 수정 요청 테스트")
    fun given_userWhoNotOwnerOfTicket_when_updateTicket_then_expectStatusCode403(){
        val ticket = Ticket.from(TICKET_REQUEST, USER_ID)

        Mockito.`when`(service.updateTicket(TICKET_REQUEST, ticket.id, USER_ID))
            .thenThrow(TicketOwnershipException(ticket.id, USER_ID))

        mvc.updateTicket(TICKET_REQUEST, ticket.id)
            .andExpect(MockMvcResultMatchers.status().isForbidden)
            .expectExceptionResponse()
    }
    @Test
    @DisplayName("티켓 스와이프 테스트")
    fun given_ID_when_swipeTicket_then_responseSwipedTicket(){
        val ticket = Ticket.from(TICKET_REQUEST, USER_ID)
        val beforeSwipeCount = ticket.swipeCount

        Mockito.`when`(service.swipeTicket(ticket.id, USER_ID))
            .thenReturn(
                TicketResponse.from(ticket.also { it.swipeAndCheckCompletion(USER_ID) })
            )

        mvc.swipeTicket(ticket.id)
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.jsonPath("swipeCount").value(beforeSwipeCount + 1))
    }

    @Test
    @DisplayName("존재하지 않는 티켓 스와이프 테스트")
    fun given_notExistTicket_when_swipeTicket_then_expectStatusCode404_and_responseExceptionResponse(){
        val ticketID = 1L

        Mockito.`when`(service.swipeTicket(ticketID, USER_ID))
            .thenThrow( TicketNotFoundException(ticketID))

        mvc.swipeTicket(ticketID)
            .andExpect(MockMvcResultMatchers.status().isNotFound)
            .expectExceptionResponse()
    }

    @Test
    @DisplayName("티켓의 소유자가 아닌 사용자의 스와이프 테스트")
    fun given_userWhoNotOwnerOfTicket_when_swipeTicket_then_expectStatusCode403_and_responseExceptionResponse(){
        val ticket = Ticket.from(TICKET_REQUEST, USER_ID)

        Mockito.`when`(service.swipeTicket(ticket.id, USER_ID))
            .thenThrow(TicketOwnershipException(ticket.id, USER_ID))

        mvc.swipeTicket(ticket.id)
            .andExpect(MockMvcResultMatchers.status().isForbidden)
            .expectExceptionResponse()
    }

    @Test
    @DisplayName("티켓 스와이프 취소 테스트")
    fun given_ID_when_cancelSwipeTicket_then_responseSwipeCanceledTicket(){
        val ticket = Ticket.from(TICKET_REQUEST, USER_ID).also { it.swipeAndCheckCompletion(USER_ID) }
        val beforeSwipeCount = ticket.swipeCount

        Mockito.`when`(service.cancelSwipeTicket(ticket.id, USER_ID))
            .thenReturn(
                ticket
                    .let {
                        it.cancelSwipeTicket(USER_ID)
                        TicketResponse.from(it)
                    }
            )

        mvc.cancelSwipeTicket(ticket.id)
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.jsonPath("swipeCount").value(beforeSwipeCount - 1))
    }

    @Test
    @DisplayName("존재하지 않는 티켓 스와이프 취소 테스트")
    fun given_notExistTicket_when_cancelSwipeTicket_then_expectStatusCode404_and_responseExceptionResponse(){
        Mockito.`when`(service.cancelSwipeTicket(Mockito.anyLong(), Mockito.anyLong()))
            .thenThrow(TicketNotFoundException(1L))

        mvc.cancelSwipeTicket(1L)
            .andExpect(MockMvcResultMatchers.status().isNotFound)
            .expectExceptionResponse()
    }

    @Test
    @DisplayName("소유자가 아닌 사용자 티켓 스와이프 취소 테스트")
    fun given_userWhoNotOwnerOfTicket_when_cancelSwipeTicket_then_expectStatusCode403_and_responseExceptionResponse(){
        val ticket = Ticket.from(TICKET_REQUEST, USER_ID).also { it.swipeAndCheckCompletion(USER_ID) }

        Mockito.`when`(service.cancelSwipeTicket(ticket.id, USER_ID))
            .thenThrow(TicketOwnershipException(ticket.id, USER_ID))

        mvc.cancelSwipeTicket(ticket.id)
            .andExpect(MockMvcResultMatchers.status().isForbidden)
    }

    @Test
    @DisplayName("미완료된 티켓 조회")
    fun given_userID_when_findIncompleteTickets_then_responseIncompleteTickets(){
        val tickets = listOf(
            TicketResponse.from((Ticket.from(TICKET_REQUEST, USER_ID))
        ))
        Mockito.`when`(service.findIncompleteTickets(USER_ID))
            .thenReturn(tickets)

        mvc.findTickets()
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.jsonPath("tickets").isArray)
            .andExpect(MockMvcResultMatchers.content().string(ObjectMapper().writeValueAsString(mapOf("tickets" to service.findIncompleteTickets(USER_ID)))))
    }

    @Test
    @DisplayName("티켓 삭제 테스트")
    fun given_id_when_deleteTicket_then_expectStatusCode204(){
        mvc.deleteTicket(1L)
            .andExpect(MockMvcResultMatchers.status().isNoContent)

        Mockito.verify(service, Mockito.times(1)).deleteTicket(Mockito.anyLong(), Mockito.anyLong())
    }

    @Test
    @DisplayName("존재하지 않는 티켓 삭제 테스트")
    fun given_notExistTicket_when_deleteTicket_then_expectStatusCode404_and_responseExceptionResponse(){
        Mockito.`when`(
            service.deleteTicket(Mockito.anyLong(), Mockito.anyLong())
        ).thenThrow(TicketNotFoundException(1L))


        mvc.deleteTicket(1L)
            .andExpect(MockMvcResultMatchers.status().isNotFound)
            .expectExceptionResponse()
    }

    @Test
    @DisplayName("소유자가 아닌 사용자의 티켓 삭제 테스트")
    fun given_userWhoNotOwner_when_deleteTicket_then_throwNotOwnerOfTicketException(){
        Mockito.`when`(
            service.deleteTicket(Mockito.anyLong(), Mockito.anyLong())
        ).thenThrow(TicketOwnershipException(1L,  USER_ID))

        mvc.deleteTicket(1L)
            .andExpect(MockMvcResultMatchers.status().isForbidden)
            .expectExceptionResponse()
    }
}
package ac.kr.smu.endTicket.ticket

import ac.kr.smu.endTicket.common.web.aop.BindExceptionAdvice
import ac.kr.smu.endTicket.common.web.test.expectBindingException
import ac.kr.smu.endTicket.constant.HttpHeaderName
import ac.kr.smu.endTicket.ticket.domain.exception.NotFoundTicketException
import ac.kr.smu.endTicket.ticket.domain.exception.NotOwnerOfTicketException
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
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
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
    private val controller: TicketController
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

        mvc.createTicketRequest(TICKET_REQUEST)
            .andExpect(MockMvcResultMatchers.status().isCreated)
            .andExpect(MockMvcResultMatchers.content().string(
                ObjectMapper().writeValueAsString(
                    TicketResponse.from(ticket)
                )
            ))
    }

    @Test
    @DisplayName("비정상적인 티켓 생성 테스트")
    fun given_invalidTicketRequest_when_createTicket_then_expectStatusCode400(){
        val request = TicketRequest(
            behavior = "",
            target = "",
            color = Ticket.Color.RED1,
            type = Ticket.Type.HEALTH,
            maxSwipeCount = Ticket.MaxSwipeCount.FIVE
        )

        mvc.createTicketRequest(request).expectBindingException()
    }

    @Test
    @DisplayName("티캣 개수 제한 이상으로 생성 테스트")
    fun given_userHasReachedTicketLimit_when_createTicket_then_expectStatusCode409(){
        Mockito
            .`when`(service.createTicket(TICKET_REQUEST, USER_ID))
            .thenAnswer { throw IllegalStateException("티켓 개수 제한 이상으로 생성할 수 없습니다.") }

        mvc.createTicketRequest(TICKET_REQUEST)
            .andExpect(MockMvcResultMatchers.status().isConflict)
            .andExpect(MockMvcResultMatchers.jsonPath("code").value(409))
            .andExpect(MockMvcResultMatchers.jsonPath("message").isString)
            .andExpect(MockMvcResultMatchers.jsonPath("detail").isString)
    }

    @Test
    @DisplayName("티켓 수정 테스트")
    fun given_ticketRequest_when_updateTicket_then_expectStatusCode200_and_responseUpdatedTicket(){
        val ticket = Ticket.from(TICKET_REQUEST, USER_ID)

        Mockito.`when`(service.updateTicket(UPDATE_REQUEST, ticket.id, USER_ID)).thenReturn(TicketResponse.from(ticket))

        mvc.updateTicketRequest(UPDATE_REQUEST, ticket.id)
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

        mvc.updateTicketRequest(updateRequest,ticket.id).expectBindingException()
    }

    @Test
    @DisplayName("존재하지 않는 티켓 수정 요청 테스트")
    fun given_notExistTicket_when_updateTicket_then_expectStatusCode404(){
        Mockito.`when`(service.updateTicket(UPDATE_REQUEST, 1L, USER_ID))
            .thenAnswer {
                throw NotFoundTicketException(1L)
            }

        mvc.updateTicketRequest(UPDATE_REQUEST, 1L).andExpect(MockMvcResultMatchers.status().isNotFound)
    }

    @Test
    @DisplayName("티켓의 소유자가 아닌 사용자의 티켓 수정 요청 테스트")
    fun given_userWhoNotOwnerOfTicket_when_updateTicket_then_expectStatusCode403(){
        val ticket = Ticket.from(TICKET_REQUEST, USER_ID)

        Mockito.`when`(service.updateTicket(TICKET_REQUEST, ticket.id, USER_ID))
            .thenAnswer {
                throw NotOwnerOfTicketException(ticket.id, USER_ID)
            }

        mvc.updateTicketRequest(TICKET_REQUEST, ticket.id)
            .andExpect(MockMvcResultMatchers.status().isForbidden)
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

        mvc.swipeTicketRequest(ticket.id)
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.jsonPath("swipeCount").value(beforeSwipeCount + 1))
    }

    @Test
    @DisplayName("존재하지 않는 티켓 스와이프 테스트")
    fun given_notExistTicket_when_swipeTicket_then_expectStatusCode404(){
        val ticketID = 1L

        Mockito.`when`(service.swipeTicket(ticketID, USER_ID))
            .thenAnswer {
                throw NotFoundTicketException(ticketID)
            }

        mvc.swipeTicketRequest(ticketID)
            .andExpect(MockMvcResultMatchers.status().isNotFound)
    }

    @Test
    @DisplayName("티켓의 소유자가 아닌 사용자의 스와이프 테스트")
    fun given_userWhoNotOwnerOfTicket_when_swipeTicket_then_expectStatusCode403(){
        val ticket = Ticket.from(TICKET_REQUEST, USER_ID)

        Mockito.`when`(service.swipeTicket(ticket.id, USER_ID))
            .thenAnswer {
                throw NotOwnerOfTicketException(ticket.id, USER_ID)
            }

        mvc.swipeTicketRequest(ticket.id)
            .andExpect(MockMvcResultMatchers.status().isForbidden)
    }

    @Test
    @DisplayName("티켓 스와이프 취소 테스트")
    fun given_ID_when_cancelSwipeTicket_then_responseSwipeCanceledTicket(){
        val ticket = Ticket.from(TICKET_REQUEST, USER_ID).also { it.swipeAndCheckCompletion(USER_ID) }
        val beforeSwipeCount = ticket.swipeCount

        Mockito.`when`(service.cancelSwipeTicket(ticket.id, USER_ID))
            .thenAnswer {
                ticket.cancelSwipeTicket(USER_ID)

                TicketResponse.from(ticket)
            }

        mvc.cancelSwipeTicketRequest(ticket.id)
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.jsonPath("swipeCount").value(beforeSwipeCount - 1))
    }

    @Test
    @DisplayName("존재하지 않는 티켓 스와이프 취소 테스트")
    fun given_notExistTicket_when_cancelSwipeTicket_then_expectStatusCode404(){
        Mockito.`when`(service.cancelSwipeTicket(Mockito.anyLong(), Mockito.anyLong()))
            .thenAnswer {
                throw NotFoundTicketException(1L)
            }

        mvc.cancelSwipeTicketRequest(1L)
            .andExpect(MockMvcResultMatchers.status().isNotFound)
    }

    @Test
    @DisplayName("소유자가 아닌 사용자 티켓 스와이프 취소 테스트")
    fun given_userWhoNotOwnerOfTicket_when_cancelSwipeTicket_then_expectStatusCode403(){
        val ticket = Ticket.from(TICKET_REQUEST, USER_ID).also { it.swipeAndCheckCompletion(USER_ID) }

        Mockito.`when`(service.cancelSwipeTicket(ticket.id, USER_ID))
            .thenAnswer { throw NotOwnerOfTicketException(ticket.id, USER_ID) }

        mvc.cancelSwipeTicketRequest(ticket.id)
            .andExpect(MockMvcResultMatchers.status().isForbidden)
    }

    @Test
    @DisplayName("미완료된 티켓 조회")
    fun given_userID_when_findIncompleteTickets_then_responseIncompleteTickets(){
        val tickets = listOf(
            TicketResponse.from((Ticket.from(TICKET_REQUEST, USER_ID))
        ))
        Mockito.`when`(service.findIncompleteTicket(USER_ID))
            .thenReturn(tickets)

        mvc.perform(
            MockMvcRequestBuilders
                .get(BASE_URI)
                .header(HttpHeaderName.USER_ID, USER_ID)
        )
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.jsonPath("tickets").isArray)
            .andExpect(MockMvcResultMatchers.content().string(ObjectMapper().writeValueAsString(mapOf("tickets" to service.findIncompleteTicket(
                USER_ID
            )))))
    }
}
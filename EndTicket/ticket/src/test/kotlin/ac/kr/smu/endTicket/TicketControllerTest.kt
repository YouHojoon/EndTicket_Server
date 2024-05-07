package ac.kr.smu.endTicket

import ac.kr.smu.endTicket.ticket.domain.exception.NotOwnerOfTicketException
import ac.kr.smu.endTicket.ticket.domain.model.Ticket
import ac.kr.smu.endTicket.ticket.service.TicketService
import ac.kr.smu.endTicket.ticket.ui.controller.TicketController
import ac.kr.smu.endTicket.ticket.ui.request.TicketRequest
import ac.kr.smu.endTicket.aop.BindExceptionAdvice
import ac.kr.smu.endTicket.ticket.domain.exception.NotFoundTicketException
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.ResultActions
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.test.web.servlet.setup.MockMvcBuilders

@WebMvcTest(controllers = [TicketController::class])
class TicketControllerTest @Autowired constructor(
    @MockBean
    private val service: TicketService,
    private val controller: TicketController
) {

    private val mvc: MockMvc =
        MockMvcBuilders.standaloneSetup(controller)
            .setControllerAdvice(BindExceptionAdvice())
            .build()

    private val USER_ID_HEADER_NAME = "X-User-ID"
    private val BASE_URI = "http://localhost:8082/tickets"
    private val USER_ID = 1L

    @Test
    @DisplayName("티켓 생성 테스트")
    fun given_ticketRequest_when_createTicket_then_expectStatusCode204_and_responseCreatedTicket(){
        val ticket = createTicket()
        val request = ticket.toTicketRequest()

        Mockito
            .`when`(service.createTicket(request, USER_ID))
            .thenReturn(ticket)

        createTicketRequest(request)
            .andExpect(MockMvcResultMatchers.status().isCreated)
            .andExpect(MockMvcResultMatchers.content().string(ObjectMapper().writeValueAsString(ticket.toTicketResponse())))
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

        createTicketRequest(request).expectBindingException()
    }

    @Test
    @DisplayName("티켓 수정 테스트")
    fun given_ticketRequest_when_updateTicket_then_expectStatusCode200_and_responseUpdatedTicket(){
        val ticket = createTicket()
        ticket.behavior = "bb"
        val request = ticket.toTicketRequest()
        Mockito.`when`(service.updateTicket(request, ticket.id, USER_ID)).thenReturn(ticket)

        updateTicketRequest(request, ticket.id)
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.content().string(ObjectMapper().writeValueAsString(ticket.toTicketResponse())))
    }

    @Test
    @DisplayName("비정상적인 티켓 수정 요청 테스트")
    fun given_invalidTicketRequest_when_updateTicket_then_expectStatusCode400(){
        val ticket = createTicket()
        ticket.behavior = ""
        val request = ticket.toTicketRequest()

        Mockito.`when`(service.updateTicket(request, ticket.id, USER_ID)).thenReturn(ticket)

        updateTicketRequest(request,ticket.id).expectBindingException()
    }

    @Test
    @DisplayName("존재하지 않는 티켓 수정 요청 테스트")
    fun given_notExistTicket_when_updateTicket_then_expectStatusCode404(){
        val ticket = createTicket()
        val request = ticket.toTicketRequest()

        Mockito.`when`(service.updateTicket(request, ticket.id, USER_ID))
            .thenAnswer {
                throw NotFoundTicketException(ticket.id)
            }

        updateTicketRequest(request, ticket.id).andExpect(MockMvcResultMatchers.status().isNotFound)
    }

    @Test
    @DisplayName("티켓의 소유자가 아닌 사용자의 티켓 수정 요청 테스트")
    fun given_userWhoNotOwnerOfTicket_when_updateTicket_then_expectStatusCode403(){
        val ticket = createTicket()
        val request = ticket.toTicketRequest()

        Mockito.`when`(service.updateTicket(request, ticket.id, USER_ID))
            .thenAnswer {
                throw NotOwnerOfTicketException(ticket.id, USER_ID)
            }

        updateTicketRequest(request, ticket.id)
            .andExpect(MockMvcResultMatchers.status().isForbidden)
    }
    @Test
    @DisplayName("티켓 스와이프 테스트")
    fun given_ID_when_swipeTicket_then_responseSwipedTicket(){
        val ticket = createTicket()
        val beforeSwipeCount = ticket.swipeCount

        Mockito.`when`(service.swipeTicket(ticket.id, USER_ID))
            .thenReturn(
                ticket.also { it.swipeAndCheckCompletion(USER_ID) }
            )

        swipeTicketRequest(ticket.id)
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

        swipeTicketRequest(ticketID)
            .andExpect(MockMvcResultMatchers.status().isNotFound)
    }

    @Test
    @DisplayName("티켓의 소유자가 아닌 사용자의 스와이프 테스트")
    fun given_userWhoNotOwnerOfTicket_when_swipeTicket_then_expectStatusCode403(){
        val ticket = createTicket()

        Mockito.`when`(service.swipeTicket(ticket.id, USER_ID))
            .thenAnswer {
                throw NotOwnerOfTicketException(ticket.id, USER_ID)
            }

        swipeTicketRequest(ticket.id)
            .andExpect(MockMvcResultMatchers.status().isForbidden)
    }
    private fun createTicketRequest(request:TicketRequest) =
        mvc.perform(
            MockMvcRequestBuilders.post(BASE_URI)
                .header(USER_ID_HEADER_NAME, USER_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(ObjectMapper().writeValueAsString(request))
                .characterEncoding(Charsets.UTF_8)
        )
    private fun updateTicketRequest(request: TicketRequest, id: Long) =
        mvc.perform(
        MockMvcRequestBuilders.put("$BASE_URI/$id")
            .header(USER_ID_HEADER_NAME, USER_ID)
            .content(ObjectMapper().writeValueAsString(request)
            ).contentType(MediaType.APPLICATION_JSON)
    )

    private fun swipeTicketRequest(id: Long) =
        mvc.perform(
            MockMvcRequestBuilders.patch("$BASE_URI/swipe/$id")
                .header(USER_ID_HEADER_NAME, USER_ID)
        )
    private fun ResultActions.expectBindingException(): ResultActions{
        return andExpect(MockMvcResultMatchers.status().isBadRequest)
            .andExpect(MockMvcResultMatchers.jsonPath("field").isString)
            .andExpect(MockMvcResultMatchers.jsonPath("code").value(400))
            .andExpect(MockMvcResultMatchers.jsonPath("message").isString)
            .andExpect(MockMvcResultMatchers.jsonPath("detail").isString)
    }
    private fun createTicket(): Ticket = Ticket(
        behavior = "b",
        target = "t",
        color = Ticket.Color.BLUE1,
        type = Ticket.Type.HEALTH,
        maxSwipeCount = Ticket.MaxSwipeCount.FIVE,
        userID = USER_ID
    )

    private fun Ticket.toTicketRequest() =
        TicketRequest(
            behavior = behavior,
            target = target,
            color = color,
            type = type,
            maxSwipeCount = maxSwipeCount
        )
}
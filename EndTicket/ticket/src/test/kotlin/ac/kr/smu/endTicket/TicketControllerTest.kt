package ac.kr.smu.endTicket

import ac.kr.smu.endTicket.ticket.domain.exception.NotOwnerOfTicketException
import ac.kr.smu.endTicket.ticket.domain.model.Ticket
import ac.kr.smu.endTicket.ticket.service.TicketService
import ac.kr.smu.endTicket.ticket.ui.controller.TicketController
import ac.kr.smu.endTicket.ticket.ui.request.TicketRequest
import ac.kr.smu.endTicket.aop.BindExceptionAdvice
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

        mvc.perform(
           MockMvcRequestBuilders.post(BASE_URI)
               .header(USER_ID_HEADER_NAME, USER_ID)
               .contentType(MediaType.APPLICATION_JSON)
               .content(ObjectMapper().writeValueAsString(request))
               .characterEncoding(Charsets.UTF_8)
        )
            .andExpect(MockMvcResultMatchers.status().isCreated)
            .andExpect(MockMvcResultMatchers.content().string(ObjectMapper().writeValueAsString(ticket)))
    }

    @Test
    @DisplayName("비정상적인 티켓 생성 테스트")
    fun given_invalidTicketRequest_when_createTicket_then_expectStatusCode400(){
        val request = TicketRequest(
            behavior = "",
            target = "",
            color = Ticket.Color.RED1,
            type = Ticket.Type.HEALTH,
            swipeCount = Ticket.SwipeCount.FIVE
        )

        mvc.perform(
            MockMvcRequestBuilders.post(BASE_URI)
                .content(ObjectMapper().writeValueAsString(request))
                .header(USER_ID_HEADER_NAME, USER_ID)
                .contentType(MediaType.APPLICATION_JSON)
        ).expectBindingException()
    }

    @Test
    @DisplayName("티켓 수정 테스트")
    fun given_ticketRequest_when_updateTicket_then_expectStatusCode200_and_responseUpdatedTicket(){
        val ticket = createTicket()
        ticket.behavior = "bb"
        val request = ticket.toTicketRequest()
        Mockito.`when`(service.updateTicket(request, ticket.id, USER_ID)).thenReturn(ticket)

        mvc.perform(
            MockMvcRequestBuilders.put(BASE_URI + "/${ticket.id}")
                .content(ObjectMapper().writeValueAsString(request)
                ).contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.content().string(ObjectMapper().writeValueAsString(ticket)))
    }

    @Test
    @DisplayName("비정상적인 티켓 수정 요청 테스트")
    fun given_invalidTicketRequest_when_updateTicket_then_expectStatusCode400(){
        val ticket = createTicket()
        ticket.behavior = ""
        val request = ticket.toTicketRequest()

        Mockito.`when`(service.updateTicket(request, ticket.id, USER_ID)).thenReturn(ticket)

        mvc.perform(
            MockMvcRequestBuilders
                .put(BASE_URI + "/${ticket.id}")
                .header(USER_ID_HEADER_NAME, USER_ID)
                .content(ObjectMapper().writeValueAsString(request))
                .contentType(MediaType.APPLICATION_JSON)
        ).expectBindingException()
    }

    @Test
    @DisplayName("id인 티켓이 존재하지 않을 때 티켓 수정 요청 테스트")
    fun given_notExistTicketForID_when_updateTicket_then_expectStatusCode404(){
        val ticket = createTicket()
        val request = ticket.toTicketRequest()
        Mockito.`when`(service.updateTicket(request, ticket.id, USER_ID))
            .thenAnswer {
                throw IllegalStateException()
            }

        mvc.perform(
            MockMvcRequestBuilders.put(BASE_URI + "/${ticket.id}")
                .content(ObjectMapper().writeValueAsString(request))
                .contentType(MediaType.APPLICATION_JSON)
                .header(USER_ID_HEADER_NAME, USER_ID)
        ).andExpect(MockMvcResultMatchers.status().isNotFound)
    }

    @Test
    @DisplayName("티켓의 소유자가 아닌 사용자의 티켓 수정 요청 테스트")
    fun given_userWhoNotOwnerOfTicket_then_expectStatusCode403(){
        val ticket = createTicket()
        val request = ticket.toTicketRequest()

        Mockito.`when`(service.updateTicket(request, ticket.id, 2L))
            .thenAnswer {
                throw NotOwnerOfTicketException()
            }

        mvc.perform(
            MockMvcRequestBuilders.put(BASE_URI + "/${ticket.id}")
                .content(ObjectMapper().writeValueAsString(request))
                .contentType(MediaType.APPLICATION_JSON)
                .header(USER_ID_HEADER_NAME, 2L)
        )
            .andExpect(MockMvcResultMatchers.status().isForbidden)

    }

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
        swipeCount = Ticket.SwipeCount.FIVE,
        userID = USER_ID
    )

    private fun Ticket.toTicketRequest() =
        TicketRequest(
            behavior = behavior,
            target = target,
            color = color,
            type = type,
            swipeCount = swipeCount
        )
}
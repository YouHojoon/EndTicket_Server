package ac.kr.smu.endTicket

import ac.kr.smu.endTicket.ticket.domain.model.Ticket
import ac.kr.smu.endTicket.ticket.service.TicketService
import ac.kr.smu.endTicket.ticket.ui.controller.TicketController
import ac.kr.smu.endTicket.ticket.ui.request.CreateTicketRequest
import aop.BindingExceptionAdvice
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
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
            .setControllerAdvice(BindingExceptionAdvice())
            .build()

    private val USER_ID_HEADER_NAME = "X-User-ID"
    private val BASE_URI = "http://localhost:8082/tickets"
    private val USER_ID = 1L

    @Test
    @DisplayName("티켓 생성 테스트")
    fun given_createTicketRequest_when_createTicket_then_expect204StatusCode_and_responseTicket(){
        val ticket = createTicket()
        val request = CreateTicketRequest(
            behavior = ticket.behavior,
            target = ticket.target,
            color = ticket.color,
            type = ticket.type,
            swipeCount = ticket.swipeCount
        )

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
    fun given_notValidCreateTicketRequest_when_createTicket_then_expect400StatusCode(){
        val request = CreateTicketRequest(
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
        )
            .andExpect(MockMvcResultMatchers.status().isBadRequest)

    }

    private fun createTicket(): Ticket = Ticket(
        behavior = "b",
        target = "t",
        color = Ticket.Color.BLUE1,
        type = Ticket.Type.HEALTH,
        swipeCount = Ticket.SwipeCount.FIVE,
        userID = USER_ID
    )

}
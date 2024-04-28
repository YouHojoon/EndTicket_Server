package ac.kr.smu.endTicket

import ac.kr.smu.endTicket.ticket.domain.model.Ticket
import ac.kr.smu.endTicket.ticket.service.TicketService
import ac.kr.smu.endTicket.ticket.ui.controller.TicketController
import ac.kr.smu.endTicket.ticket.ui.request.CreateTicketRequest
import com.fasterxml.jackson.databind.ObjectMapper
import org.apache.catalina.User
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.post
import org.springframework.test.web.servlet.request
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers

@WebMvcTest(controllers = [TicketController::class])
@AutoConfigureMockMvc(addFilters = false)
class TicketControllerTest @Autowired constructor(
    private val mvc: MockMvc,

    @MockBean
    private val service: TicketService
) {
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

    private fun createTicket(): Ticket = Ticket(
        behavior = "b",
        target = "t",
        color = Ticket.Color.BLUE1,
        type = Ticket.Type.HEALTH,
        swipeCount = Ticket.SwipeCount.FIVE,
        userID = USER_ID
    )

}
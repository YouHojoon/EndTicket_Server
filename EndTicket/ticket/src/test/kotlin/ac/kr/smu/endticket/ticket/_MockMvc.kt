package ac.kr.smu.endticket.ticket

import ac.kr.smu.endticket.common.constant.HttpHeaderName
import ac.kr.smu.endticket.ticket.ui.request.TicketRequest
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders

const val BASE_URI = "http://localhost:8082/tickets"

fun MockMvc.createTicket(request: TicketRequest, userId: Long = TicketTestParameters.USER_ID) =
    perform(
        MockMvcRequestBuilders.post(BASE_URI)
            .header(HttpHeaderName.USER_ID, userId)
            .contentType(MediaType.APPLICATION_JSON)
            .content(ObjectMapper().writeValueAsString(request))
            .characterEncoding(Charsets.UTF_8)
    )
fun MockMvc.updateTicket(request: TicketRequest, id: Long, userId: Long = TicketTestParameters.USER_ID) =
   perform(
        MockMvcRequestBuilders.put("$BASE_URI/$id")
            .header(HttpHeaderName.USER_ID, userId)
            .content(ObjectMapper().writeValueAsString(request))
            .contentType(MediaType.APPLICATION_JSON)
    )

fun MockMvc.swipeTicket(id: Long, userId: Long = TicketTestParameters.USER_ID) =
    perform(
        MockMvcRequestBuilders.patch("$BASE_URI/swipe/$id")
            .header(HttpHeaderName.USER_ID, userId)
    )

fun MockMvc.cancelSwipeTicket(id: Long, userId: Long = TicketTestParameters.USER_ID) =
    perform(
        MockMvcRequestBuilders.delete("$BASE_URI/swipe/$id")
            .header(HttpHeaderName.USER_ID, userId)
    )

fun MockMvc.deleteTicket(id:Long, userId: Long = TicketTestParameters.USER_ID) =
    perform(
        MockMvcRequestBuilders.delete("$BASE_URI/$id")
            .header(HttpHeaderName.USER_ID, userId)
    )

fun MockMvc.findTickets() = perform(
    MockMvcRequestBuilders
        .get(BASE_URI)
        .header(HttpHeaderName.USER_ID, TicketTestParameters.USER_ID)
)

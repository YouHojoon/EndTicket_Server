package ac.kr.smu.endTicket

import ac.kr.smu.endTicket.constant.HttpHeaderName
import ac.kr.smu.endTicket.ticket.ui.request.TicketRequest
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders

const val BASE_URI = "http://localhost:8082/tickets"

fun MockMvc.createTicketRequest(request: TicketRequest, userID: Long = USER_ID) =
    perform(
        MockMvcRequestBuilders.post(BASE_URI)
            .header(HttpHeaderName.USER_ID, userID)
            .contentType(MediaType.APPLICATION_JSON)
            .content(ObjectMapper().writeValueAsString(request))
            .characterEncoding(Charsets.UTF_8)
    )
fun MockMvc.updateTicketRequest(request: TicketRequest, id: Long) =
   perform(
        MockMvcRequestBuilders.put("$BASE_URI/$id")
            .header(HttpHeaderName.USER_ID, USER_ID)
            .content(ObjectMapper().writeValueAsString(request))
            .contentType(MediaType.APPLICATION_JSON)
    )

fun MockMvc.swipeTicketRequest(id: Long) =
    perform(
        MockMvcRequestBuilders.patch("$BASE_URI/swipe/$id")
            .header(HttpHeaderName.USER_ID, USER_ID)
    )

fun MockMvc.cancelSwipeTicketRequest(id: Long) =
    perform(
        MockMvcRequestBuilders.delete("$BASE_URI/swipe/$id")
            .header(HttpHeaderName.USER_ID, USER_ID)
    )
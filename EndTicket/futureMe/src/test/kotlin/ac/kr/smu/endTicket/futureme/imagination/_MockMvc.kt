package ac.kr.smu.endTicket.futureme.imagination

import ac.kr.smu.endTicket.constant.HttpHeaderName
import ac.kr.smu.endTicket.futureme.ui.request.ImaginationRequest
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders

const val BASE_URL = "http://localhost:8084/imaginations"

fun MockMvc.findImaginations(userId: Long = USER_ID) = perform(
    MockMvcRequestBuilders
        .get(BASE_URL)
        .header(HttpHeaderName.USER_ID, userId)
)

fun MockMvc.createImagination(request: ImaginationRequest, userId: Long = USER_ID) = perform(
    MockMvcRequestBuilders
        .post(BASE_URL)
        .header(HttpHeaderName.USER_ID, userId)
        .contentType(MediaType.APPLICATION_JSON)
        .content(ObjectMapper().writeValueAsString(request))
)

fun MockMvc.updateImagination(request: ImaginationRequest,id: Long ,userId: Long = USER_ID) = perform(
    MockMvcRequestBuilders
        .put("$BASE_URL/$id")
        .header(HttpHeaderName.USER_ID, userId)
        .contentType(MediaType.APPLICATION_JSON)
        .content(ObjectMapper().writeValueAsString(request))
)

fun MockMvc.deleteImagination(id: Long, userId: Long = USER_ID) = perform(
    MockMvcRequestBuilders.delete("$BASE_URL/$id")
        .header(HttpHeaderName.USER_ID, userId)
)

fun MockMvc.completeImagination(id: Long, userId: Long = USER_ID) = perform(
    MockMvcRequestBuilders.post("$BASE_URL/complete/$id")
        .header(HttpHeaderName.USER_ID, userId)
)
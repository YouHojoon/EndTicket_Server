package ac.kr.smu.endTicket.futureMe.imagination

import ac.kr.smu.endTicket.constant.HttpHeaderName
import ac.kr.smu.endTicket.futureMe.ui.request.ImaginationRequest
import ac.kr.smu.endTicket.futureMe.ui.response.ImaginationResponse
import com.fasterxml.jackson.databind.ObjectMapper
import org.mockito.Mock
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders

const val BASE_URL = "http://localhost:8084/imaginations"

fun MockMvc.findImaginations(userID: Long = USER_ID) = perform(
    MockMvcRequestBuilders
        .get(BASE_URL)
        .header(HttpHeaderName.USER_ID, userID)
)

fun MockMvc.createImagination(request: ImaginationRequest, userID: Long = USER_ID) = perform(
    MockMvcRequestBuilders
        .post(BASE_URL)
        .header(HttpHeaderName.USER_ID, userID)
        .contentType(MediaType.APPLICATION_JSON)
        .content(ObjectMapper().writeValueAsString(request))
)

fun MockMvc.updateImagination(request: ImaginationRequest,id: Long ,userID: Long = USER_ID) = perform(
    MockMvcRequestBuilders
        .put("$BASE_URL/$id")
        .header(HttpHeaderName.USER_ID, userID)
        .contentType(MediaType.APPLICATION_JSON)
        .content(ObjectMapper().writeValueAsString(request))
)

fun MockMvc.deleteImagination(id: Long, userID: Long = USER_ID) = perform(
    MockMvcRequestBuilders.delete("$BASE_URL/$id")
        .header(HttpHeaderName.USER_ID, userID)
)
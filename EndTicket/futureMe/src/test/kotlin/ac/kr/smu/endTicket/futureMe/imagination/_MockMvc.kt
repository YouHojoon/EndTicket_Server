package ac.kr.smu.endTicket.futureMe.imagination

import ac.kr.smu.endTicket.constant.HttpHeaderName
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders

const val BASE_URL = "http://localhost:8084/imaginations"

fun MockMvc.findImaginations(userID: Long = USER_ID) = perform(
    MockMvcRequestBuilders
        .get(BASE_URL)
        .header(HttpHeaderName.USER_ID, userID)
)
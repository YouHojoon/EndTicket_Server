package ac.kr.smu.endTicket

import ac.kr.smu.endTicket.constant.HttpHeaderName
import ac.kr.smu.endTicket.user.ui.request.RegisterNicknameRequest
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders

const val BASE_URL = "http://localhost:8080"
const val USER_ID = 1L

fun MockMvc.registerNickname(request: RegisterNicknameRequest) =
    perform(MockMvcRequestBuilders.post("$BASE_URL/users/nickname")
        .header(HttpHeaderName.USER_ID, USER_ID)
        .contentType(MediaType.APPLICATION_JSON)
        .content(ObjectMapper().writeValueAsString(request))
    )
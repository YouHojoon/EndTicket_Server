package ac.kr.smu.endticket.user

import ac.kr.smu.endticket.common.constant.HttpHeaderName
import ac.kr.smu.endticket.user.ui.request.NicknameRegisterRequest
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders

const val BASE_URL = "http://localhost:8080"
const val USER_ID = 1L

fun MockMvc.registerNickname(request: NicknameRegisterRequest, id: Long = USER_ID) =
    perform(MockMvcRequestBuilders.post("$BASE_URL/users/nickname")
        .header(HttpHeaderName.USER_ID, id)
        .contentType(MediaType.APPLICATION_JSON)
        .content(ObjectMapper().writeValueAsString(request))
    )

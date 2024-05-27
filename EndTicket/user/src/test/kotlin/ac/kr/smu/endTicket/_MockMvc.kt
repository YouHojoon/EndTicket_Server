package ac.kr.smu.endTicket

import ac.kr.smu.endTicket.constant.HttpHeaderName
import ac.kr.smu.endTicket.user.ui.request.RegisterNicknameRequest
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.ResultActions
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers

const val BASE_URL = "http://localhost:8080"
const val USER_ID = 1L

fun MockMvc.registerNickname(request: RegisterNicknameRequest, id: Long = USER_ID) =
    perform(MockMvcRequestBuilders.post("$BASE_URL/users/nickname")
        .header(HttpHeaderName.USER_ID, id)
        .contentType(MediaType.APPLICATION_JSON)
        .content(ObjectMapper().writeValueAsString(request))
    )

fun ResultActions.expectBindingException() =
    andExpect(MockMvcResultMatchers.status().isBadRequest)
        .andExpect(MockMvcResultMatchers.jsonPath("field").isString)
        .andExpect(MockMvcResultMatchers.jsonPath("code").value(400))
        .andExpect(MockMvcResultMatchers.jsonPath("message").isString)
        .andExpect(MockMvcResultMatchers.jsonPath("detail").isString)
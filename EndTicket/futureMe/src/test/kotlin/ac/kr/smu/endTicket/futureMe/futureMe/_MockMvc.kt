package ac.kr.smu.endTicket.futureMe.futureMe

import ac.kr.smu.endTicket.constant.HttpHeaderName
import ac.kr.smu.endTicket.futureMe.domain.futureMe.model.Character
import ac.kr.smu.endTicket.futureMe.domain.futureMe.model.FutureMe
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.ResultActions
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders

const val BASE_URL = "http://localhost:8084/future-me"

fun MockMvc.findFutureMe(userID: Long = USER_ID): ResultActions = perform(
    MockMvcRequestBuilders
        .get(BASE_URL)
        .header(HttpHeaderName.USER_ID, userID)
)

fun MockMvc.findCharacterImage(type: Character.Type): ResultActions = perform(
    MockMvcRequestBuilders
        .get("$BASE_URL/characters/${type.name.lowercase()}")
)

fun MockMvc.createFutureMe(type: Character.Type): ResultActions = perform(
    MockMvcRequestBuilders
        .post(BASE_URL)
        .header(HttpHeaderName.USER_ID, USER_ID)
        .contentType(MediaType.APPLICATION_JSON)
        .content(
            ObjectMapper().writeValueAsString(mapOf("type" to type))
        )
)


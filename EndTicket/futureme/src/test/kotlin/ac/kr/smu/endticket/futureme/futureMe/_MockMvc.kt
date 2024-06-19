package ac.kr.smu.endticket.futureme.futureMe

import ac.kr.smu.endticket.common.constant.HttpHeaderName
import ac.kr.smu.endticket.futureme.domain.futureme.model.Character
import ac.kr.smu.endticket.futureme.ui.request.CreateFutureMeRequest
import ac.kr.smu.endticket.futureme.ui.request.UpdateFutureMeRequest
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.ResultActions
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders

const val BASE_URL = "http://localhost:8084/future-me"

fun MockMvc.findFutureMe(userId: Long = USER_ID): ResultActions = perform(
    MockMvcRequestBuilders
        .get(BASE_URL)
        .header(HttpHeaderName.USER_ID, userId)
)

fun MockMvc.findCharacterImage(type: Character.Type): ResultActions = perform(
    MockMvcRequestBuilders
        .get("$BASE_URL/characters/${type.name.lowercase()}")
)

fun MockMvc.createFutureMe(request: CreateFutureMeRequest): ResultActions = perform(
    MockMvcRequestBuilders
        .post(BASE_URL)
        .header(HttpHeaderName.USER_ID, USER_ID)
        .contentType(MediaType.APPLICATION_JSON)
        .content(
            ObjectMapper().writeValueAsString(request)
        )
)

fun MockMvc.updateFutureMe(request: UpdateFutureMeRequest): ResultActions = perform(
    MockMvcRequestBuilders
        .patch(BASE_URL)
        .header(HttpHeaderName.USER_ID, USER_ID)
        .contentType(MediaType.APPLICATION_JSON)
        .content(
            ObjectMapper().writeValueAsString(request)
        )
)
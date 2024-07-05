package ac.kr.smu.endticket.futureme.futureme

import ac.kr.smu.endticket.common.constant.HttpHeaderName
import ac.kr.smu.endticket.common.web.enum.CharacterType
import ac.kr.smu.endticket.futureme.ui.request.CreateFutureMeRequest
import ac.kr.smu.endticket.futureme.ui.request.UpdateFutureMeRequest
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.ResultActions
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders

const val BASE_URL = "http://localhost:8084/future-me"

fun MockMvc.findFutureMe(userId: Long = FutureMeTestParameters.USER_ID): ResultActions =
    perform(
        MockMvcRequestBuilders
            .get(BASE_URL)
            .header(HttpHeaderName.USER_ID, userId),
    )

fun MockMvc.findCharacterImage(type: CharacterType): ResultActions =
    perform(
        MockMvcRequestBuilders
            .get("$BASE_URL/characters/${type.name.lowercase()}"),
    )

fun MockMvc.createFutureMe(request: CreateFutureMeRequest): ResultActions =
    perform(
        MockMvcRequestBuilders
            .post(BASE_URL)
            .header(HttpHeaderName.USER_ID, FutureMeTestParameters.USER_ID)
            .contentType(MediaType.APPLICATION_JSON)
            .content(
                ObjectMapper().writeValueAsString(request),
            ),
    )

fun MockMvc.updateFutureMe(request: UpdateFutureMeRequest): ResultActions =
    perform(
        MockMvcRequestBuilders
            .patch(BASE_URL)
            .header(HttpHeaderName.USER_ID, FutureMeTestParameters.USER_ID)
            .contentType(MediaType.APPLICATION_JSON)
            .content(
                ObjectMapper().writeValueAsString(request),
            ),
    )

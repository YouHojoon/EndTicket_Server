package ac.kr.smu.endTicket.futureMe.futureMe

import ac.kr.smu.endTicket.constant.HttpHeaderName
import ac.kr.smu.endTicket.futureMe.domain.futureMe.model.Character
import ac.kr.smu.endTicket.futureMe.domain.futureMe.model.FutureMe
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


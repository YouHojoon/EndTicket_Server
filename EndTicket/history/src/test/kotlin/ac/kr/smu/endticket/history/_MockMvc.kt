package ac.kr.smu.endticket.history

import ac.kr.smu.endticket.common.constant.HttpHeaderName
import ac.kr.smu.endticket.history.domain.model.History
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders

private const val BASE_URL = "http://localhost:8085/histories"

fun MockMvc.findHistories(type: History.Type) = perform(
    MockMvcRequestBuilders
        .get("$BASE_URL/${type.name.lowercase()}")
        .header(HttpHeaderName.USER_ID, HistoryTestParameters.USER_ID)
)
package ac.kr.smu.endticket.auth

import ac.kr.smu.endTicket.auth.domain.model.SocialType
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.ResultActions
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders

const val BASE_URL = "http://localhost:8081/auth"

fun MockMvc.createToken(socialType: SocialType = SOCIAL_TYPE, code: String = AUTHORIZATION_CODE): ResultActions = perform(
    MockMvcRequestBuilders
        .post("$BASE_URL/sns?socialType=$socialType&code=$code")
)

fun MockMvc.reissueToken(refreshToken: String? = null) = perform(
    MockMvcRequestBuilders
        .post("$BASE_URL/reissue-token")
        .contentType(MediaType.APPLICATION_JSON)
        .content(ObjectMapper().writeValueAsString(mapOf("refreshToken" to refreshToken)))
)
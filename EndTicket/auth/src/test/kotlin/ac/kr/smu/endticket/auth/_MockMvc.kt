package ac.kr.smu.endticket.auth

import ac.kr.smu.endticket.auth.domain.model.SocialType
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.post

const val BASE_URL = "http://localhost:8081/auth"

fun MockMvc.createToken(
    socialType: SocialType = AuthTestParameters.SOCIAL_TYPE,
    code: String = AuthTestParameters.AUTHORIZATION_CODE,
) = post("$BASE_URL/token?socialType=$socialType&code=$code")

fun MockMvc.reissueToken(refreshToken: String? = AuthTestParameters.REFRESH_TOKEN) =
    post("$BASE_URL/reissue-token") {
        contentType = MediaType.APPLICATION_JSON
        content = ObjectMapper().writeValueAsString(mapOf("refreshToken" to refreshToken))
    }

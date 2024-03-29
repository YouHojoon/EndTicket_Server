package ac.kr.smu.endTicket

import ac.kr.smu.endTicket.infra.config.SecurityConfig
import ac.kr.smu.endTicket.user.domain.exception.UserAlreadyExistException
import ac.kr.smu.endTicket.user.domain.model.User
import ac.kr.smu.endTicket.user.domain.service.UserService
import ac.kr.smu.endTicket.user.ui.controller.UserController
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers


@WebMvcTest(controllers = [UserController::class])
@Import(SecurityConfig::class)
@AutoConfigureMockMvc
class UserControllerTest @Autowired constructor(
    private val mockMvc: MockMvc,
    private val objectMapper: ObjectMapper,

    @MockBean
    private val service: UserService,
) {

    @Test
    @DisplayName("회원가입 테스트")
    fun test_createUser(){
        //정상 회원
        val body1 = objectMapper.writeValueAsString(
            mapOf("socialType" to "KAKAO", "socialUserNumber" to "1")
        )

        //파라미터 조건 불충분
        val body2 = objectMapper.writeValueAsString(
            mapOf("socialType" to "KAKAO", "socialUserNumber" to "")
        )

        mockMvc
            .perform(
            MockMvcRequestBuilders.post("/users")
                .content(body1)
                .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(MockMvcResultMatchers.status().isCreated)

        mockMvc.perform(MockMvcRequestBuilders.post("/users")
            .content(body2)
            .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(MockMvcResultMatchers.status().is4xxClientError)
            .andExpect(MockMvcResultMatchers.jsonPath("field").isString)
            .andExpect(MockMvcResultMatchers.jsonPath("message").isString)

        //이미 회원가입 된 회원
        Mockito.`when`(service.createUser(User(User.SocialType.KAKAO, "1")))
            .thenThrow(UserAlreadyExistException())

        mockMvc
            .perform(
                MockMvcRequestBuilders.post("/users")
                    .content(body1)
                    .contentType(MediaType.APPLICATION_JSON)
            ).andExpect(MockMvcResultMatchers.status().isConflict)
    }

    @Test
    @DisplayName("사용자 id 반환 테스트")
    fun test_getUserId() {
        Mockito
            .`when`(service.findIdBySocialTypeAndSocialUserNumber(User.SocialType.KAKAO,"1"))
            .thenReturn(1)

        Mockito.`when`(service.findIdBySocialTypeAndSocialUserNumber(User.SocialType.KAKAO,"2"))
            .thenReturn(null)

        mockMvc
            .perform(
                MockMvcRequestBuilders
                    .get("/users/kakao/1")
            )
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.jsonPath("userID").value(1))

        mockMvc.perform(MockMvcRequestBuilders.get("/users/kakao/2"))
            .andExpect(MockMvcResultMatchers.status().isNotFound)
    }
}
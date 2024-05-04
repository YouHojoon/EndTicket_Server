package ac.kr.smu.endTicket

import ac.kr.smu.endTicket.user.config.SecurityConfig
import ac.kr.smu.endTicket.user.domain.model.User
import ac.kr.smu.endTicket.user.domain.service.UserService
import ac.kr.smu.endTicket.user.ui.controller.UserController
import ac.kr.smu.endTicket.aop.BindExceptionAdvice
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.BeforeEach
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
import org.springframework.test.web.servlet.ResultActions
import org.springframework.test.web.servlet.ResultMatcher
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.test.web.servlet.setup.MockMvcBuilders


@WebMvcTest(controllers = [UserController::class])
class UserControllerTest @Autowired constructor(
    private val controller: UserController,
    @MockBean
    private val service: UserService
) {
    private val mockMvc: MockMvc = MockMvcBuilders.standaloneSetup(controller).setControllerAdvice(BindExceptionAdvice()).build()
    private val BASE_URL = "http://localhost:8080"
    private val USER_ID = 1L



    @Test
    @DisplayName("닉네임 등록")
    fun given_nickname_when_updateNickname_then_updateNicknameOfUser(){
        val nickname = "nickname"

        mockMvc.perform(
            MockMvcRequestBuilders
                .post("$BASE_URL/users/nickname")
                .header("X-User-ID", USER_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(ObjectMapper().writeValueAsString(mapOf("nickname" to nickname)))
        ).andExpect(MockMvcResultMatchers.status().isNoContent)

        Mockito.verify(service, Mockito.times(1)).registerNickname(nickname, USER_ID)
    }

    @Test
    @DisplayName("닉네임 조건 테스트")
    fun given_invalidNickname_when_updateNickname_then_expect400Error(){
        val lowLengthNickname = "a"
        val patternMismatchedNickname = "$^&@(a"

        val baseBuilder = MockMvcRequestBuilders.post("$BASE_URL/users/nickname")
            .header("X-User-ID", USER_ID)
            .contentType(MediaType.APPLICATION_JSON)

        fun ResultActions.expect(): ResultActions{
            return this.andExpect(MockMvcResultMatchers.status().isBadRequest)
                .andExpect(MockMvcResultMatchers.jsonPath("field").isString)
                .andExpect(MockMvcResultMatchers.jsonPath("code").value(400))
                .andExpect(MockMvcResultMatchers.jsonPath("message").isString)
                .andExpect(MockMvcResultMatchers.jsonPath("detail").isString)
        }

        mockMvc.perform(
                baseBuilder
                .content(ObjectMapper().writeValueAsString(mapOf("nickname" to lowLengthNickname)))
        ).expect()

        mockMvc.perform(
            baseBuilder
                .content(ObjectMapper().writeValueAsString(mapOf("nickname" to patternMismatchedNickname)))
        ).expect()
    }
}
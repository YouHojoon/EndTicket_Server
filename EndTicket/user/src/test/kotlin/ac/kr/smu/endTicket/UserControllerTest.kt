package ac.kr.smu.endTicket

import ac.kr.smu.endTicket.infra.config.SecurityConfig
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
    private val service: UserService
) {

    @Test
    @DisplayName("회원가입 테스트")
    fun createUser(){
        val body1 = objectMapper.writeValueAsString(
            mapOf("nickname" to "test", "email" to "test@test.com", "socialType" to "KAKAO", "socialUserNumber" to "1")
        )
        val body2 = objectMapper.writeValueAsString(
            mapOf("nickname" to "test12312312312", "email" to "tes.com", "socialType" to "KAKAO", "socialUserNumber" to "1")
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
            .andExpect(MockMvcResultMatchers.jsonPath("validCode").isString)
    }

    @Test
    @DisplayName("이메일 중복확인 테스트")
    fun checkUserExistenceByEmail(){
        Mockito
            .`when`(service.checkUserExistenceByEmail("test@test.com"))
            .thenReturn(true)

        Mockito.`when`(service.checkUserExistenceByEmail("test1@test.com"))
            .thenReturn(false)

        mockMvc
            .perform(MockMvcRequestBuilders
                .get("/users/test@test.com")
            )
            .andExpect(MockMvcResultMatchers.status().isOk)

        mockMvc.perform(MockMvcRequestBuilders.get("/users/test1@test.com"))
            .andExpect(MockMvcResultMatchers.status().isNotFound)
    }
}
package ac.kr.smu.endTicket

import ac.kr.smu.endTicket.user.domain.service.UserService
import ac.kr.smu.endTicket.user.ui.controller.UserController
import ac.kr.smu.endTicket.common.web.aop.BindExceptionAdvice
import ac.kr.smu.endTicket.constant.HttpHeaderName
import ac.kr.smu.endTicket.test.expectBindingException
import ac.kr.smu.endTicket.user.domain.exception.NotFoundUserException
import ac.kr.smu.endTicket.user.ui.request.RegisterNicknameRequest
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.ResultActions
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.test.web.servlet.setup.MockMvcBuilders


@WebMvcTest(controllers = [UserController::class])
class UserControllerTest @Autowired constructor(
    private val controller: UserController,

    @MockBean
    private val service: UserService
) {
    private val mockMvc: MockMvc =
        MockMvcBuilders
        .standaloneSetup(controller)
        .setControllerAdvice(BindExceptionAdvice())
        .build()

    companion object{
        private const val NICKNAME = "닉네임"
    }

    @Test
    @DisplayName("닉네임 등록")
    fun given_nickname_when_updateNickname_then_updateNicknameOfUser(){
        val request = RegisterNicknameRequest(NICKNAME)

        mockMvc
            .registerNickname(request)
            .andExpect(MockMvcResultMatchers.status().isNoContent)

        Mockito
            .verify(service, Mockito.times(1))
            .registerNickname(request, USER_ID)
    }

    @Test
    @DisplayName("부적절한 닉네임 등록 테스트")
    fun given_invalidNickname_when_updateNickname_then_expect400Error(){
        val lowLengthNickname = RegisterNicknameRequest("a")
        val patternMismatchedNickname = RegisterNicknameRequest("$^&@(a")

        mockMvc.registerNickname(lowLengthNickname).expectBindingException()
        mockMvc.registerNickname(patternMismatchedNickname).expectBindingException()
    }

    @Test
    @DisplayName("존재하지 않는 사용자의 닉네임 등록 테스트")
    fun given_notExistUser_when_registerNickname_then_expectStatusCode404(){
        val request = RegisterNicknameRequest(NICKNAME)

        Mockito.`when`(service.registerNickname(request, USER_ID))
            .thenAnswer { throw NotFoundUserException(USER_ID) }

        mockMvc
            .registerNickname(request)
            .andExpect(MockMvcResultMatchers.status().isNotFound)
    }

    @Test
    @DisplayName("닉네임이 등록되어 있는 사용자의 닉네임 등록 테스트")
    fun given_userWithNicknameAlreadyRegistered_when_registerNickname_then_expectStatus409(){
        val request = RegisterNicknameRequest(NICKNAME)

        Mockito.`when`(service.registerNickname(request, USER_ID))
            .thenAnswer { throw IllegalStateException() }

        mockMvc
            .registerNickname(request)
            .andExpect(MockMvcResultMatchers.status().isConflict)
    }
}
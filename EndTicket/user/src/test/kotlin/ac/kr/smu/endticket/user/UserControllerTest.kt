package ac.kr.smu.endticket.user

import ac.kr.smu.endticket.common.web.aop.BindExceptionAdvice
import ac.kr.smu.endticket.common.web.test.expectBindException
import ac.kr.smu.endticket.common.web.test.expectExceptionResponse
import ac.kr.smu.endticket.user.service.UserService
import ac.kr.smu.endticket.user.ui.controller.UserController
import ac.kr.smu.endticket.user.ui.request.NicknameRegisterRequest
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.test.web.servlet.setup.MockMvcBuilders


@WebMvcTest(controllers = [UserController::class])
class UserControllerTest @Autowired constructor(
    controller: UserController,

    @MockBean
    private val service: UserService
) {
    private val mockMvc: MockMvc =
        MockMvcBuilders
        .standaloneSetup(controller)
        .setControllerAdvice(BindExceptionAdvice())
        .build()

    @Test
    @DisplayName("닉네임 등록")
    fun given_nickname_when_updateNickname_then_updateNicknameOfUser(){
        val request = NicknameRegisterRequest(UserTestParameters.NICKNAME)

        mockMvc
            .registerNickname(request)
            .andExpect(MockMvcResultMatchers.status().isNoContent)

        Mockito
            .verify(service)
            .registerNickname(request, USER_ID)
    }

    @ParameterizedTest
    @DisplayName("부적절한 닉네임 등록 테스트")
    @MethodSource("${UserTestParameters.PATH}#provideInvalidNicknameRegisterRequest")
    fun given_invalidNickname_when_updateNickname_then_responseBindException(request: NicknameRegisterRequest){
        mockMvc.registerNickname(request).expectBindException()
    }

    @ParameterizedTest
    @DisplayName("비정상적인 사용자의 닉네임 등록 테스트")
    @MethodSource("${UserTestParameters.PATH}#provideNicknameRegisterRequestAndException")
    fun given_invalidUser_when_registerNickname_then_responseExceptionResponseWithExpectedStatus(
        request: NicknameRegisterRequest,
        exception: Throwable,
        status: Int
    ){
        Mockito.`when`(service.registerNickname(request, USER_ID))
            .thenAnswer { throw exception}

        mockMvc
            .registerNickname(request)
            .andExpect(MockMvcResultMatchers.status().`is`(status))
            .expectExceptionResponse()
    }
}
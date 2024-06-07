package ac.kr.smu.endTicket.futureMe.futureMe

import ac.kr.smu.endTicket.common.web.aop.BindExceptionAdvice
import ac.kr.smu.endTicket.common.web.test.expectExceptionResponse
import ac.kr.smu.endTicket.futureMe.domain.futureMe.exception.NotFoundFutureMeException
import ac.kr.smu.endTicket.futureMe.domain.futureMe.model.Character
import ac.kr.smu.endTicket.futureMe.domain.futureMe.model.FutureMe
import ac.kr.smu.endTicket.futureMe.service.FutureMeService
import ac.kr.smu.endTicket.futureMe.ui.controller.FutureMeController
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.test.web.servlet.setup.MockMvcBuilders

@WebMvcTest(controllers = [FutureMeController::class])
class FutureMeControllerTest @Autowired constructor(
    @MockBean
    private val service: FutureMeService,
    private val controller: FutureMeController
) {
    private val mvc = MockMvcBuilders
        .standaloneSetup(controller)
        .setControllerAdvice(BindExceptionAdvice())
        .build()

    @Test
    @DisplayName("미래의 나 조회 테스트")
    fun given_user_when_findFutureMe_then_responseFutureMe(){
        val futureMe = FutureMe(Character.Type.CHEESE, USER_ID)

        Mockito.`when`(service.findFutureMe(USER_ID))
            .thenReturn(futureMe)

        mvc.findFutureMe()
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.content().string(ObjectMapper().writeValueAsString(futureMe)))
    }

    @Test
    @DisplayName("존재하지 않는 미래의 나 조회 테스트")
    fun given_userHasNotFutureMe_when_findFutureMe_then_expectStatusCode404(){
        Mockito.`when`(service.findFutureMe(USER_ID))
            .thenAnswer { throw NotFoundFutureMeException(USER_ID) }

        mvc.findFutureMe()
            .andExpect(MockMvcResultMatchers.status().isNotFound)
            .expectExceptionResponse()
    }
}
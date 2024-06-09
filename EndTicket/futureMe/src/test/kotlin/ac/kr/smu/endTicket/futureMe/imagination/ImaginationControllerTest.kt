package ac.kr.smu.endTicket.futureMe.imagination

import ac.kr.smu.endTicket.common.web.test.expectExceptionResponse
import ac.kr.smu.endTicket.futureMe.domain.imagination.model.Imagination
import ac.kr.smu.endTicket.futureMe.service.ImaginationService
import ac.kr.smu.endTicket.futureMe.ui.controller.ImaginationController
import ac.kr.smu.endTicket.futureMe.ui.response.ImaginationResponse
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext

@WebMvcTest(
    controllers = [ImaginationController::class]
)
class ImaginationControllerTest @Autowired constructor(
    @MockBean
    private val service: ImaginationService,
    private val ctx: WebApplicationContext
) {
    private val mvc: MockMvc =
        MockMvcBuilders
            .webAppContextSetup(ctx)
            .build()

    @Test
    @DisplayName("상상해보기 조회 테스트")
    fun given_user_when_findImaginations_then_responseImaginations(){
        val imaginations = setOf(
            ImaginationResponse.from(Imagination.from(request, USER_ID))
        )

        Mockito.`when`(service.findImaginations(USER_ID))
            .thenReturn(imaginations)

        mvc.findImaginations()
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.content().string(ObjectMapper().writeValueAsString(mapOf("imaginations" to imaginations))))
    }

    @Test
    @DisplayName("상상해보기 생성 테스트")
    fun given_request_when_createImagination_then_responseCreatedImagination(){
        val imagination = ImaginationResponse.from(Imagination.from(request, USER_ID))

        Mockito.`when`(service.createImagination(request, USER_ID))
            .thenReturn(imagination)

        mvc
            .createImagination(request)
            .andExpect(MockMvcResultMatchers.status().isCreated)
            .andExpect(MockMvcResultMatchers.content().string(ObjectMapper().writeValueAsString(imagination)))
    }

    @Test
    @DisplayName("최대 개수 이상으로 상상해보기 생성 테스트")
    fun given_requestExceedImaginationLimit_when_createImagination_then_expectStatusCode409(){
        Mockito.`when`(service.createImagination(request, USER_ID))
            .thenAnswer { throw IllegalStateException("") }

        mvc.createImagination(request)
            .andExpect(MockMvcResultMatchers.status().isConflict)
            .expectExceptionResponse()
    }
}
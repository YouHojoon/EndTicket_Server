package ac.kr.smu.endTicket.futureMe.imagination

import ac.kr.smu.endTicket.common.web.aop.BindExceptionAdvice
import ac.kr.smu.endTicket.common.web.test.andReturn
import ac.kr.smu.endTicket.common.web.test.expectBindingException
import ac.kr.smu.endTicket.common.web.test.expectExceptionResponse
import ac.kr.smu.endTicket.futureMe.domain.imagination.model.Imagination
import ac.kr.smu.endTicket.futureMe.domain.imagination.repository.ImaginationRepository
import ac.kr.smu.endTicket.futureMe.service.FutureMeEventService
import ac.kr.smu.endTicket.futureMe.service.ImaginationService
import ac.kr.smu.endTicket.futureMe.ui.controller.ImaginationController
import ac.kr.smu.endTicket.futureMe.ui.request.ImaginationRequest
import ac.kr.smu.endTicket.futureMe.ui.response.ImaginationResponse
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration
import org.springframework.boot.autoconfigure.transaction.TransactionAutoConfiguration
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import kotlin.test.AfterTest

@SpringBootTest(
    classes = [
        ImaginationController::class,
        ImaginationService::class,
        FutureMeEventService::class,
        DataSourceAutoConfiguration::class,
        HibernateJpaAutoConfiguration::class,
        TransactionAutoConfiguration::class
    ]
)
@EnableJpaRepositories("ac.kr.smu.endTicket.futureMe.domain.imagination.repository")
@EntityScan("ac.kr.smu.endTicket.futureMe.domain.imagination.model")
class ImaginationIntegrationTest @Autowired constructor(
    private val repo: ImaginationRepository,
    controller: ImaginationController
) {
    private val mvc: MockMvc = MockMvcBuilders.standaloneSetup(controller).setControllerAdvice(BindExceptionAdvice()).build()

    @AfterTest
    fun reset(){
        repo.deleteAll()
    }

    @Test
    @DisplayName("상상해보기 생성 테스트")
    fun given_request_when_createImagination_then_responseCreatedImagination() {
        mvc
            .createImagination(request)
            .andExpect(MockMvcResultMatchers.status().isCreated)
            .andExpect(MockMvcResultMatchers.jsonPath("behavior").value(request.behavior))
            .andExpect(MockMvcResultMatchers.jsonPath("target").value(request.target))
            .andExpect(MockMvcResultMatchers.jsonPath("color").value(request.color.name))
    }

    @Test
    @DisplayName("상상해보기 조회 테스트")
    fun given_user_when_findImaginations_then_responseImaginations(){
        mvc.createImagination(request)

        mvc.findImaginations()
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.jsonPath("imaginations").isNotEmpty)
    }


    @Test
    @DisplayName("비정상적인 상상해보기 생성 테스트")
    fun given_invalidRequest_when_createImagination_then_expectStatusCode400_and_responseBindExceptionResponse(){
        mvc.createImagination(invalidBehaviorRequest)
            .expectBindingException()
        mvc.createImagination(invalidTargetRequest)
            .expectBindingException()
    }

    @Test
    @DisplayName("최대 개수 이상으로 상상해보기 생성 테스트")
    fun given_requestExceedImaginationLimit_when_createImagination_then_expectStatusCode409_and_responseExceptionResponse(){
        repeat(6){
            mvc.createImagination(request)
        }

        mvc.createImagination(request)
            .andExpect(MockMvcResultMatchers.status().isConflict)
            .expectExceptionResponse()
    }

    @Test
    @DisplayName("상상해보기 수정 테스트")
    fun given_request_when_updateImagination_then_responseUpdatedImagination(){
        val request = ImaginationRequest(
            behavior = "new behav",
            target = "new target",
            color = Imagination.Color.GRAY2
        )
        val imagination = mvc.createImagination(request).andReturn<ImaginationResponse>()

        mvc.updateImagination(request, imagination.id, USER_ID)
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.jsonPath("behavior").value(request.behavior))
            .andExpect(MockMvcResultMatchers.jsonPath("target").value(request.target))
            .andExpect(MockMvcResultMatchers.jsonPath("color").value(request.color.name))

    }

    @Test
    @DisplayName("비정상적인 상상해보기 수정 테스트")
    fun given_invalidRequest_when_updateImagination_then_expectStatusCode400_and_responseBindExceptionResponse(){
        val imagination = mvc.createImagination(request).andReturn<ImaginationResponse>()

        mvc.updateImagination(invalidBehaviorRequest, imagination.id, USER_ID)
            .expectBindingException()
        mvc.updateImagination(invalidTargetRequest, imagination.id, USER_ID)
            .expectBindingException()
    }

    @Test
    @DisplayName("존재하지 않는 상상해보기 수정 테스트")
    fun given_notExistImagination_when_updateImagination_then_expectStatusCode404_and_responseExceptionResponse(){
        mvc.updateImagination(request,1L, USER_ID)
            .andExpect(MockMvcResultMatchers.status().isNotFound)
            .expectExceptionResponse()
    }

    @Test
    @DisplayName("소유자가 아닌 사용자 상상해보기 수정 테스트")
    fun given_userWhoNotOwner_when_updateImagination_then_expectStatusCode404_and_responseExceptionResponse(){
        val imagination = mvc.createImagination(request).andReturn<ImaginationResponse>()

        mvc.updateImagination(request,imagination.id, 2L)
            .andExpect(MockMvcResultMatchers.status().isForbidden)
            .expectExceptionResponse()
    }

    @Test
    @DisplayName("상상해보기 삭제 테스트")
    fun given_id_when_deleteImagination_then_expectStatusCode204(){
        val imagination = mvc.createImagination(request).andReturn<ImaginationResponse>()

        mvc.deleteImagination(imagination.id)
            .andExpect(MockMvcResultMatchers.status().isNoContent)
    }

    @Test
    @DisplayName("존재하지 않는 상상해보기 삭제 테스트")
    fun given_notExistImagination_when_deleteImagination_then_expectStatusCode404_and_responseExceptionResponse(){
        mvc.deleteImagination(1L)
            .andExpect(MockMvcResultMatchers.status().isNotFound)
            .expectExceptionResponse()
    }

    @Test
    @DisplayName("소유자가 아닌 사용자의 상상해보기 삭제 테스트")
    fun given_userWhoNotOwner_when_deleteImagination_then_expectStatusCode403_and_responseExceptionResponse(){
        val imagination = mvc.createImagination(request).andReturn<ImaginationResponse>()

        mvc.deleteImagination(imagination.id, 2L)
            .andExpect(MockMvcResultMatchers.status().isForbidden)
            .expectExceptionResponse()
    }
}

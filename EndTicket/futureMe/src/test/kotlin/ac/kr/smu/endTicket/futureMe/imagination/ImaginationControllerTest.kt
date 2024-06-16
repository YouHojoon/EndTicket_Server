package ac.kr.smu.endTicket.futureMe.imagination

import ac.kr.smu.endTicket.common.web.aop.BindExceptionAdvice
import ac.kr.smu.endTicket.common.web.test.expectBindingException
import ac.kr.smu.endTicket.common.web.test.expectExceptionResponse
import ac.kr.smu.endTicket.futureMe.domain.imagination.exception.ImaginationNotFoundException
import ac.kr.smu.endTicket.futureMe.domain.imagination.exception.ImaginationOwnershipException
import ac.kr.smu.endTicket.futureMe.domain.imagination.model.Imagination
import ac.kr.smu.endTicket.futureMe.service.FutureMeEventService
import ac.kr.smu.endTicket.futureMe.service.ImaginationService
import ac.kr.smu.endTicket.futureMe.ui.controller.ImaginationController
import ac.kr.smu.endTicket.futureMe.ui.request.ImaginationRequest
import ac.kr.smu.endTicket.futureMe.ui.response.ImaginationResponse
import ac.kr.smu.endTicket.test.mockAny
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.test.web.servlet.setup.MockMvcBuilders

@WebMvcTest(
    controllers = [ImaginationController::class]
)
@MockBean(JpaMetamodelMappingContext::class)
class ImaginationControllerTest @Autowired constructor(
    @MockBean
    private val service: ImaginationService,
    @MockBean
    private val eventService: FutureMeEventService,
    controller: ImaginationController
) {
    private val mvc: MockMvc = MockMvcBuilders.standaloneSetup(controller).setControllerAdvice(BindExceptionAdvice()).build()

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
        Mockito.`when`(service.createImagination(request, USER_ID))
            .thenThrow(IllegalStateException("") )


        mvc.createImagination(request)
            .andExpect(MockMvcResultMatchers.status().isConflict)
            .expectExceptionResponse()
    }

    @Test
    @DisplayName("상상해보기 수정 테스트")
    fun given_request_when_updateImagination_then_responseUpdatedImagination(){
        val id = 1L
        val request = ImaginationRequest(
            behavior = "new behav",
            target = "new target",
            color = Imagination.Color.GRAY2
        )
        val response =  ImaginationResponse.from(Imagination.from(request, USER_ID))

        Mockito.`when`(
            service.updateImagination(request, id, USER_ID)
        ).thenReturn(response)

        mvc.updateImagination(request, id, USER_ID)
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.content().string(ObjectMapper().writeValueAsString(response)))

        Mockito.verify(service, Mockito.times(1)).updateImagination(request,id, USER_ID)
    }

    @Test
    @DisplayName("비정상적인 상상해보기 수정 테스트")
    fun given_invalidRequest_when_updateImagination_then_expectStatusCode400_and_responseBindExceptionResponse(){
        val id = 1L

       mvc.updateImagination(invalidBehaviorRequest, id, USER_ID)
           .expectBindingException()
        mvc.updateImagination(invalidTargetRequest, id, USER_ID)
            .expectBindingException()
    }

    @Test
    @DisplayName("존재하지 않는 상상해보기 수정 테스트")
    fun given_notExistImagination_when_updateImagination_then_expectStatusCode404_and_responseExceptionResponse(){
        val id = 1L

        Mockito.`when`(
            service.updateImagination(request, id, USER_ID)
        ).thenThrow(ImaginationNotFoundException(id))

        mvc.updateImagination(request,id, USER_ID)
            .andExpect(MockMvcResultMatchers.status().isNotFound)
            .expectExceptionResponse()
    }

    @Test
    @DisplayName("소유자가 아닌 사용자 상상해보기 수정 테스트")
    fun given_userWhoNotOwner_when_updateImagination_then_expectStatusCode404_and_responseExceptionResponse(){
        val id = 1L

        Mockito.`when`(
            service.updateImagination(request, id, USER_ID)
        ).thenThrow(ImaginationOwnershipException(id, USER_ID))

        mvc.updateImagination(request,id, USER_ID)
            .andExpect(MockMvcResultMatchers.status().isForbidden)
            .expectExceptionResponse()
    }

    @Test
    @DisplayName("상상해보기 삭제 테스트")
    fun given_id_when_deleteImagination_then_expectStatusCode204(){
        mvc.deleteImagination(1L)
            .andExpect(MockMvcResultMatchers.status().isNoContent)

        Mockito.verify(service, Mockito.times(1)).deleteImagination(1L, USER_ID)
    }

    @Test
    @DisplayName("존재하지 않는 상상해보기 삭제 테스트")
    fun given_notExistImagination_when_deleteImagination_then_expectStatusCode404_and_responseExceptionResponse(){
        Mockito.`when`(service.deleteImagination(Mockito.anyLong(), Mockito.anyLong()))
            .thenThrow(ImaginationNotFoundException(1L))

        mvc.deleteImagination(1L)
            .andExpect(MockMvcResultMatchers.status().isNotFound)
            .expectExceptionResponse()
    }
    @Test
    @DisplayName("소유자가 아닌 사용자의 상상해보기 삭제 테스트")
    fun given_userWhoNotOwner_when_deleteImagination_then_expectStatusCode403_and_responseExceptionResponse(){
        Mockito.`when`(service.deleteImagination(Mockito.anyLong(), Mockito.anyLong()))
            .thenThrow(ImaginationOwnershipException(1L, USER_ID))

        mvc.deleteImagination(1L)
            .andExpect(MockMvcResultMatchers.status().isForbidden)
            .expectExceptionResponse()
    }

    @Test
    @DisplayName("상상해보기 완료 테스트")
    fun given_id_when_completeImagination_then_expectStatusCode204_and_publishImaginationCompletionEvent(){
        Mockito.`when`(service.completeImagination(1L, USER_ID)).then { eventService.publishEvent(mockAny()) }

        mvc.completeImagination(1L)
            .andExpect(MockMvcResultMatchers.status().isNoContent)

        Mockito.verify(service, Mockito.times(1)).completeImagination(1L, USER_ID)
        Mockito.verify(eventService, Mockito.times(1)).publishEvent(mockAny())
    }

    @Test
    @DisplayName("존재하지 않는 상상해보기 완료 테스트")
    fun given_notExistImagination_when_completeImagination_then_throwNotFoundImaginationException(){
        val id = 1L
        Mockito.`when`(service.completeImagination(id, USER_ID))
            .thenThrow(ImaginationNotFoundException(id))

        mvc.completeImagination(id)
            .andExpect(MockMvcResultMatchers.status().isNotFound)
            .expectExceptionResponse()
    }

    @Test
    @DisplayName("소유자가 아닌 사용자의 상상해보기 완료 테스트")
    fun given_userWhoNotOwner_when_completeImagination_then_throwNotOwnerOfImagination(){
        val id = 1L
        Mockito.`when`(service.completeImagination(id, USER_ID))
            .thenThrow( ImaginationOwnershipException(id, USER_ID))

        mvc.completeImagination(id)
            .andExpect(MockMvcResultMatchers.status().isForbidden)
            .expectExceptionResponse()
    }
}

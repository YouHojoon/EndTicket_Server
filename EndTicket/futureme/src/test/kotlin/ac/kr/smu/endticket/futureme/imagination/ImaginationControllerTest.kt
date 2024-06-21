package ac.kr.smu.endticket.futureme.imagination

import ac.kr.smu.endticket.futureme.domain.imagination.exception.ImaginationOwnershipException
import ac.kr.smu.endticket.common.test.mockAny
import ac.kr.smu.endticket.common.web.aop.BindExceptionAdvice
import ac.kr.smu.endticket.common.web.test.expectBindException
import ac.kr.smu.endticket.common.web.test.expectExceptionResponse
import ac.kr.smu.endticket.futureme.service.FutureMeEventService
import ac.kr.smu.endticket.futureme.service.ImaginationService
import ac.kr.smu.endticket.futureme.ui.controller.ImaginationController
import ac.kr.smu.endticket.futureme.ui.request.ImaginationRequest
import ac.kr.smu.endticket.futureme.ui.response.ImaginationResponse
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
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

    @ParameterizedTest
    @DisplayName("상상해보기 조회 테스트")
    @MethodSource("${ImaginationParameters.PATH}#provideImaginationResponses")
    fun given_userId_when_findImaginations_then_responseImaginations(imaginationResponses: Set<ImaginationResponse>){
        Mockito
            .`when`(service.findImaginations(ImaginationParameters.USER_ID))
            .thenReturn(imaginationResponses)

        mvc.findImaginations()
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers
                .content()
                .string(ObjectMapper().writeValueAsString(mapOf("imaginations" to imaginationResponses))))
    }

    @ParameterizedTest
    @DisplayName("상상해보기 생성 테스트")
    @MethodSource("${ImaginationParameters.PATH}#provideImaginationResponseAndRequest")
    fun given_requestAndUserId_when_createImagination_then_responseCreatedImagination(response: ImaginationResponse, request: ImaginationRequest){
        Mockito.`when`(service.createImagination(request, ImaginationParameters.USER_ID))
            .thenReturn(response)

        mvc
            .createImagination(request)
            .andExpect(MockMvcResultMatchers.status().isCreated)
            .andExpect(MockMvcResultMatchers.content().string(ObjectMapper().writeValueAsString(response)))
    }

    @ParameterizedTest
    @DisplayName("비정상적인 상상해보기 생성 요청 테스트")
    @MethodSource("${ImaginationParameters.PATH}#provideInvalidImaginationRequest")
    fun given_invalidRequest_when_createImagination_then_expectStatusCode400_and_responseBindExceptionResponse(request: ImaginationRequest){
        mvc.createImagination(request)
            .expectBindException()
    }

    @Test
    @DisplayName("최대 개수 이상으로 상상해보기 생성 테스트")
    fun given_requestExceedImaginationLimit_when_createImagination_then_responseExceptionResponseWithStatus409(){
        Mockito.`when`(service.createImagination(ImaginationParameters.REQUEST, ImaginationParameters.USER_ID))
            .thenThrow(IllegalStateException("") )

        mvc.createImagination(ImaginationParameters.REQUEST)
            .andExpect(MockMvcResultMatchers.status().isConflict)
            .expectExceptionResponse()
    }

    @ParameterizedTest
    @DisplayName("상상해보기 수정 테스트")
    @MethodSource("${ImaginationParameters.PATH}#provideImaginationResponseAndRequest")
    fun given_request_when_updateImagination_then_responseUpdatedImagination(response: ImaginationResponse, request: ImaginationRequest){
        Mockito.`when`(
            service.updateImagination(request, response.id, ImaginationParameters.USER_ID)
        ).thenReturn(response)

        mvc.updateImagination(request, response.id, ImaginationParameters.USER_ID)
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.content().string(ObjectMapper().writeValueAsString(response)))

        Mockito.verify(service, Mockito.times(1)).updateImagination(request,response.id, ImaginationParameters.USER_ID)
    }

    @ParameterizedTest
    @DisplayName("비정상적인 수정 요청으로 상상해보기 수정 테스트")
    @MethodSource("${ImaginationParameters.PATH}#provideInvalidImaginationRequest")
    fun given_invalidRequest_when_updateImagination_then_responseBindExceptionResponseWithStatus400(request: ImaginationRequest){
       mvc.updateImagination(request, 1L, ImaginationParameters.USER_ID)
           .expectBindException()
    }

    @ParameterizedTest
    @DisplayName("비정상적인 상상해보기 수정 테스트")
    @MethodSource("${ImaginationParameters.PATH}#provideInvalidIdAndRequest")
    fun given_invalidId_when_updateImagination_then_responseExceptionResponseWithExpectedStatus(
        request: ImaginationRequest,
        id: Long,
        userId: Long,
        exception: Throwable,
        status: Int
    ){
        Mockito.`when`(
            service.updateImagination(request, id, userId)
        ).thenThrow(exception)

        mvc.updateImagination(request,id, userId)
            .andExpect(MockMvcResultMatchers.status().`is`(status))
            .expectExceptionResponse()
    }

    @Test
    @DisplayName("상상해보기 삭제 테스트")
    fun given_id_when_deleteImagination_then_expectStatus204(){
        val id = 1L
        mvc.deleteImagination(id)
            .andExpect(MockMvcResultMatchers.status().isNoContent)

        Mockito.verify(service, Mockito.times(1)).deleteImagination(id, ImaginationParameters.USER_ID)
    }

    @ParameterizedTest
    @DisplayName("비정상적인 상상해보기 삭제 테스트")
    @MethodSource("${ImaginationParameters.PATH}#provideInvalidIdAndException")
    fun given_invalidId_when_deleteImagination_then_responseExceptionResponseWithExpectedStatus(
        id: Long,
        userId: Long,
        exception: Throwable,
        status: Int
    ){
        Mockito
            .`when`(service.deleteImagination(id, userId))
            .thenThrow(exception)

        mvc.deleteImagination(id, userId)
            .andExpect(MockMvcResultMatchers.status().`is`(status))
            .expectExceptionResponse()
    }

    @Test
    @DisplayName("상상해보기 완료 테스트")
    fun given_id_when_completeImagination_then_expectStatusCode204AndPublishImaginationCompletionEvent(){
        val id = 1L
        Mockito.`when`(service.completeImagination(id, ImaginationParameters.USER_ID))
            .then { eventService.publishEvent(mockAny()) }

        mvc.completeImagination(id)
            .andExpect(MockMvcResultMatchers.status().isNoContent)

        Mockito.verify(service).completeImagination(id, ImaginationParameters.USER_ID)
        Mockito.verify(eventService).publishEvent(mockAny())
    }

    @ParameterizedTest
    @DisplayName("비정상적인 상상해보기 완료 테스트")
    @MethodSource("${ImaginationParameters.PATH}#provideInvalidIdAndException")
    fun given_invalidId_when_completeImagination_then_responseExceptionResponseWithExpectedStatus(
        id: Long,
        userId: Long,
        exception: Throwable,
        status: Int
    ){
        Mockito.`when`(service.completeImagination(id, userId))
            .thenThrow(exception)

        mvc.completeImagination(id, userId)
            .andExpect(MockMvcResultMatchers.status().`is`(status))
            .expectExceptionResponse()
    }
}

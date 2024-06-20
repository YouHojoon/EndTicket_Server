package ac.kr.smu.endticket.futureme.futureme

import ac.kr.smu.endticket.common.web.aop.BindExceptionAdvice
import ac.kr.smu.endticket.common.web.enum.CharacterType
import ac.kr.smu.endticket.common.web.test.expectBindingException
import ac.kr.smu.endticket.common.web.test.expectExceptionResponse
import ac.kr.smu.endticket.futureme.domain.converter.CharacterTypeConverter
import ac.kr.smu.endticket.futureme.domain.futureme.exception.FutureMeNotFoundException
import ac.kr.smu.endticket.futureme.domain.futureme.model.Character
import ac.kr.smu.endticket.futureme.domain.futureme.model.FutureMe
import ac.kr.smu.endticket.futureme.futureme.USER_ID
import ac.kr.smu.endticket.futureme.service.FutureMeService
import ac.kr.smu.endticket.futureme.ui.controller.FutureMeController
import ac.kr.smu.endticket.futureme.ui.request.CreateFutureMeRequest
import ac.kr.smu.endticket.futureme.ui.request.UpdateFutureMeRequest
import ac.kr.smu.endticket.futureme.ui.response.FutureMeResponse
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext
import org.springframework.format.support.FormattingConversionService
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.test.web.servlet.setup.MockMvcBuilders

@WebMvcTest(controllers = [FutureMeController::class])
@MockBean(JpaMetamodelMappingContext::class)
class FutureMeControllerTest @Autowired constructor(
    @MockBean
    private val service: FutureMeService,
    controller: FutureMeController
) {
    private val mvc = MockMvcBuilders
        .standaloneSetup(controller)
        .setControllerAdvice(BindExceptionAdvice())
        .setConversionService(FormattingConversionService().also { it.addConverter(CharacterTypeConverter()) })
        .build()

    @Test
    @DisplayName("미래의 나 조회 테스트")
    fun given_user_when_findFutureMe_then_responseFutureMe(){
        val futureMe = FutureMe.from(CreateFutureMeRequest(CharacterType.CHEESE), USER_ID)

        Mockito.`when`(service.findFutureMe(USER_ID))
            .thenReturn(FutureMeResponse.from(futureMe))

        mvc.findFutureMe()
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.content().string(ObjectMapper().writeValueAsString(FutureMeResponse.from(futureMe))))
    }

    @Test
    @DisplayName("존재하지 않는 미래의 나 조회 테스트")
    fun given_userHasNotFutureMe_when_findFutureMe_then_expectStatusCode404_and_responseExceptionResponse(){
        Mockito.`when`(service.findFutureMe(USER_ID))
            .thenAnswer { throw FutureMeNotFoundException(USER_ID) }

        mvc.findFutureMe()
            .andExpect(MockMvcResultMatchers.status().isNotFound)
            .expectExceptionResponse()
    }

    @Test
    @DisplayName("캐릭터 이미지 조회 테스트")
    fun given_type_when_findCharacterImage_then_responseCharacterImage(){
        val type = CharacterType.CHEESE

        mvc.findCharacterImage(type)
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.valueOf("image/svg+xml")))
            .andExpect(MockMvcResultMatchers.content().bytes(type.imageResource.contentAsByteArray))
    }

    @Test
    @DisplayName("존재하지 않는 캐릭터 이미지 조회 테스트")
    fun given_notExistType_when_findCharacterImage_then_expectStatusCode404_and_responseExceptionResponse(){
        mvc.perform(
            MockMvcRequestBuilders.get("$BASE_URL/characters/xxx")
        ).andExpect(MockMvcResultMatchers.status().isBadRequest)
            .expectExceptionResponse()
    }

    @Test
    @DisplayName("미래의 나 생성 테스트")
    fun given_type_when_createFutureMe_then_responseCreatedFutureMe(){
        val request = CreateFutureMeRequest(CharacterType.CHEESE)

        Mockito.`when`(service.createFutureMe(request, USER_ID))
            .thenReturn(FutureMeResponse.from(FutureMe.from(request, USER_ID)))

        mvc
            .createFutureMe(request)
            .andExpect(MockMvcResultMatchers.status().isCreated)
            .andExpect(MockMvcResultMatchers.jsonPath("character.type").value(request.type.name))
    }

    @Test
    @DisplayName("미래의 나 제목 수정 테스트")
    fun given_request_when_updateFutureMe_then_responseUpdatedFutureMe(){
        val request = UpdateFutureMeRequest("테스트")
        val futureMe = FutureMe.from(CreateFutureMeRequest(CharacterType.CHEESE), USER_ID)
        futureMe.update(request)

        Mockito.`when`(service.updateFutureMe(request, USER_ID))
            .thenReturn(FutureMeResponse.from(futureMe))

        mvc
            .updateFutureMe(request)
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.jsonPath("title").value(request.title))
    }

    @Test
    @DisplayName("미래의 나가 없는 사용자의 수정 테스트")
    fun given_userDoesNotHasFutureMee_when_updateFutureMe_then_expectStatusCode404_and_responseExceptionResponse(){
        val request = UpdateFutureMeRequest("테스트")

        Mockito
            .`when`(service.updateFutureMe(request, USER_ID))
            .thenAnswer { throw FutureMeNotFoundException(USER_ID) }

        mvc
            .updateFutureMe(request)
            .andExpect(MockMvcResultMatchers.status().isNotFound)
            .expectExceptionResponse()
    }

    @Test
    @DisplayName("미래의 나 길이 초과된 제목으로 등록/변경 테스트")
    fun given_requestWithExceedMaxLength_when_updateFutureMe_then_expectStatusCode400_and_responseBindingExceptionResponse(){
        val request = UpdateFutureMeRequest("미래의 나 길이 초과된 제목 테스트")

        mvc.updateFutureMe(request)
            .expectBindingException()
    }
    @Test
    @DisplayName("이미 미래의 나가 존재할 때 미래의 나 생성 테스트")
    fun given_requestWithAlreadyExistFutureMe_when_createFutureMe_then_expectStatusCode409_and_responseExceptionResponse(){
        val request = CreateFutureMeRequest(CharacterType.CHEESE)

        Mockito.`when`(service.createFutureMe(request, USER_ID))
            .thenAnswer { throw IllegalStateException("") }

        mvc.createFutureMe(request)
            .andExpect(MockMvcResultMatchers.status().isConflict)
            .expectExceptionResponse()
    }
}
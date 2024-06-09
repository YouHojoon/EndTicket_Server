package ac.kr.smu.endTicket.futureMe.futureMe

import ac.kr.smu.endTicket.common.web.test.expectBindingException
import ac.kr.smu.endTicket.common.web.test.expectExceptionResponse
import ac.kr.smu.endTicket.futureMe.domain.futureMe.exception.NotFoundFutureMeException
import ac.kr.smu.endTicket.futureMe.domain.futureMe.model.Character
import ac.kr.smu.endTicket.futureMe.domain.futureMe.model.FutureMe
import ac.kr.smu.endTicket.futureMe.service.FutureMeService
import ac.kr.smu.endTicket.futureMe.ui.controller.FutureMeController
import ac.kr.smu.endTicket.futureMe.ui.request.FutureMeCharacterRequest
import ac.kr.smu.endTicket.futureMe.ui.request.UpdateFutureMeTitleRequest
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext

@WebMvcTest(controllers = [FutureMeController::class])
class FutureMeControllerTest @Autowired constructor(
    @MockBean
    private val service: FutureMeService,
    private val controller: FutureMeController,
    private val ctx: WebApplicationContext
) {
    private val mvc = MockMvcBuilders
        .webAppContextSetup(ctx)
        .build()

    @Test
    @DisplayName("미래의 나 조회 테스트")
    fun given_user_when_findFutureMe_then_responseFutureMe(){
        val futureMe = FutureMe.from(FutureMeCharacterRequest(Character.Type.CHEESE), USER_ID)

        Mockito.`when`(service.findFutureMe(USER_ID))
            .thenReturn(futureMe)

        mvc.findFutureMe()
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.content().string(ObjectMapper().writeValueAsString(futureMe)))
    }

    @Test
    @DisplayName("존재하지 않는 미래의 나 조회 테스트")
    fun given_userHasNotFutureMe_when_findFutureMe_then_expectStatusCode404_and_responseExceptionResponse(){
        Mockito.`when`(service.findFutureMe(USER_ID))
            .thenAnswer { throw NotFoundFutureMeException(USER_ID) }

        mvc.findFutureMe()
            .andExpect(MockMvcResultMatchers.status().isNotFound)
            .expectExceptionResponse()
    }

    @Test
    @DisplayName("캐릭터 이미지 조회 테스트")
    fun given_type_when_findCharacterImage_then_responseCharacterImage(){
        val type = Character.Type.CHEESE

        mvc.findCharacterImage(type)
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.valueOf("image/svg+xml")))
            .andExpect(MockMvcResultMatchers.content().bytes(type.imageResource.contentAsByteArray))
    }

    @Test
    @DisplayName("존재하지 않는 캐릭터 이미지 조회 테스트")
    fun given_notExistType_when_findCharacterImage_then_expectStatusCode404_and_responseExceptionResponse(){
        mvc.perform(
            MockMvcRequestBuilders.get("$BASE_URL/future-me/characters/xxx")
        ).andExpect(MockMvcResultMatchers.status().isNotFound)
            .expectExceptionResponse()
    }

    @Test
    @DisplayName("미래의 나 생성 테스트")
    fun given_type_when_createFutureMe_then_responseCreatedFutureMe(){
        val request = FutureMeCharacterRequest(Character.Type.CHEESE)

        Mockito.`when`(service.createFutureMe(request, USER_ID))
            .thenReturn(FutureMe.from(request, USER_ID))

        mvc
            .createFutureMe(request)
            .andExpect(MockMvcResultMatchers.status().isCreated)
            .andExpect(MockMvcResultMatchers.jsonPath("character.type").value(request.type.name))
    }

    @Test
    @DisplayName("미래의 나 제목 등록/변경 테스트")
    fun given_request_when_updateTitle_then_responseUpdatedFutureMe(){
        val request = UpdateFutureMeTitleRequest("테스트")
        val futureMe = FutureMe.from(FutureMeCharacterRequest(Character.Type.CHEESE), USER_ID)
        futureMe.updateTitle(request)

        Mockito.`when`(service.updateTitle(request, USER_ID))
            .thenReturn(futureMe)

        mvc
            .updateTitle(request)
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.jsonPath("title").value(request.title))
    }

    @Test
    @DisplayName("미래의 나가 없는 사용자의 제목 등록/변경 테스트")
    fun given_userHasNotFutureMe_when_updateTitle_then_expectStatusCode404_and_responseExceptionResponse(){
        val request = UpdateFutureMeTitleRequest("테스트")

        Mockito
            .`when`(service.updateTitle(request, USER_ID))
            .thenAnswer { throw NotFoundFutureMeException(USER_ID) }

        mvc
            .updateTitle(request)
            .andExpect(MockMvcResultMatchers.status().isNotFound)
            .expectExceptionResponse()
    }

    @Test
    @DisplayName("미래의 나 길이 초과된 제목으로 등록/변경 테스트")
    fun given_requestWithExceedMaxLength_when_updateTitle_then_expectStatusCode400_and_responseBindingExceptionResponse(){
        val request = UpdateFutureMeTitleRequest("미래의 나 길이 초과된 제목 테스트")

        mvc.updateTitle(request)
            .expectBindingException()
    }
    @Test
    @DisplayName("미래의 나 캐릭터 변경")
    fun given_request_when_updateCharacter_then_responseUpdatedFutureMe(){
        val futureMe = FutureMe.from(FutureMeCharacterRequest(Character.Type.VEGA), USER_ID)
        val request = FutureMeCharacterRequest(Character.Type.CHEESE)
        futureMe.updateCharacter(request, USER_ID)

        Mockito.`when`(service.updateCharacter(request, USER_ID))
            .thenReturn(futureMe)

        mvc.updateCharacter(request)
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.jsonPath("character.type").value(request.type.name))
    }

    @Test
    @DisplayName("미래의 나가 없는 사용자의 캐릭터 변경 테스트")
    fun given_userHasNotFutureMe_when_updateCharacter_then_expectStatusCode404_and_responseExceptionResponse(){
        val request = FutureMeCharacterRequest(Character.Type.CHEESE)

        Mockito.`when`(service.updateCharacter(request, USER_ID))
            .thenAnswer { throw NotFoundFutureMeException(USER_ID) }

        mvc.updateCharacter(request)
            .andExpect(MockMvcResultMatchers.status().isNotFound)
            .expectExceptionResponse()
    }
}
package ac.kr.smu.endTicket.futureMe.futureMe

import ac.kr.smu.endTicket.common.web.aop.BindExceptionAdvice
import ac.kr.smu.endTicket.common.web.test.andReturn
import ac.kr.smu.endTicket.common.web.test.expectBindingException
import ac.kr.smu.endTicket.common.web.test.expectExceptionResponse
import ac.kr.smu.endTicket.futureMe.domain.converter.CharacterTypeConverter
import ac.kr.smu.endTicket.futureMe.domain.futureMe.model.Character
import ac.kr.smu.endTicket.futureMe.domain.futureMe.model.FutureMe
import ac.kr.smu.endTicket.futureMe.domain.futureMe.repository.FutureMeRepository
import ac.kr.smu.endTicket.futureMe.service.FutureMeService
import ac.kr.smu.endTicket.futureMe.ui.controller.FutureMeController
import ac.kr.smu.endTicket.futureMe.ui.request.FutureMeCharacterRequest
import ac.kr.smu.endTicket.futureMe.ui.request.UpdateFutureMeTitleRequest
import ac.kr.smu.endTicket.futureMe.ui.response.FutureMeResponse
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration
import org.springframework.boot.autoconfigure.transaction.TransactionAutoConfiguration
import org.springframework.boot.autoconfigure.web.format.DateTimeFormatters
import org.springframework.boot.autoconfigure.web.format.WebConversionService
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.core.convert.ConversionFailedException
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.format.support.FormattingConversionService
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext
import org.springframework.web.servlet.handler.AbstractHandlerMapping
import org.springframework.web.servlet.mvc.method.annotation.ExceptionHandlerExceptionResolver
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping
import org.springframework.web.servlet.mvc.support.DefaultHandlerExceptionResolver
import kotlin.test.AfterTest


@SpringBootTest(
    classes = [
        FutureMeController::class,
        FutureMeService::class,
        TransactionAutoConfiguration::class,
        DataSourceAutoConfiguration::class,
        HibernateJpaAutoConfiguration::class,
    ]
)
@EnableJpaRepositories("ac.kr.smu.endTicket.futureMe.domain.futureMe.repository")
@EntityScan("ac.kr.smu.endTicket.futureMe.domain.futureMe.model")
class FutureMeIntegrationTest @Autowired constructor(
    private val repo: FutureMeRepository,
    controller: FutureMeController,
) {
    private val mvc = MockMvcBuilders
        .standaloneSetup(controller)
        .setControllerAdvice(BindExceptionAdvice())
        .setConversionService(FormattingConversionService().also { it.addConverter(CharacterTypeConverter()) })
        .build()

    @AfterTest
    fun reset(){
        repo.deleteAll()
    }

    @Test
    @DisplayName("미래의 나 조회 테스트")
    fun given_user_when_findFutureMe_then_responseFutureMe() {
        val futureMe = mvc
            .createFutureMe(FutureMeCharacterRequest(Character.Type.CHEESE))
            .andReturn<FutureMe>()

        mvc.findFutureMe()
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.content().string(ObjectMapper().writeValueAsString(FutureMeResponse.from(futureMe))))
    }

    @Test
    @DisplayName("존재하지 않는 미래의 나 조회 테스트")
    fun given_userHasNotFutureMe_when_findFutureMe_then_expectStatusCode404_and_responseExceptionResponse() {
        mvc.findFutureMe()
            .andExpect(MockMvcResultMatchers.status().isNotFound)
            .expectExceptionResponse()
    }

    @Test
    @DisplayName("캐릭터 이미지 조회 테스트")
    fun given_type_when_findCharacterImage_then_responseCharacterImage() {
        val type = Character.Type.CHEESE

        mvc.findCharacterImage(type)
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.valueOf("image/svg+xml")))
            .andExpect(MockMvcResultMatchers.content().bytes(type.imageResource.contentAsByteArray))
    }

    @Test
    @DisplayName("존재하지 않는 캐릭터 이미지 조회 테스트")
    fun given_notExistType_when_findCharacterImage_then_expectStatusCode404_and_responseExceptionResponse() {
        mvc.perform(
            MockMvcRequestBuilders.get("$BASE_URL/characters/xxx")
        )
            .andExpect(MockMvcResultMatchers.status().isBadRequest)
            .expectExceptionResponse()
    }

    @Test
    @DisplayName("미래의 나 생성 테스트")
    fun given_type_when_createFutureMe_then_responseCreatedFutureMe() {
        val request = FutureMeCharacterRequest(Character.Type.CHEESE)

        mvc
            .createFutureMe(request)
            .andExpect(MockMvcResultMatchers.status().isCreated)
            .andExpect(MockMvcResultMatchers.jsonPath("character.type").value(request.type.name))
    }

    @Test
    @DisplayName("미래의 나 제목 등록/변경 테스트")
    fun given_request_when_updateTitle_then_responseUpdatedFutureMe() {
        val request = UpdateFutureMeTitleRequest("테스트")

        mvc.createFutureMe(FutureMeCharacterRequest(Character.Type.CHEESE)).andReturn<FutureMe>()

        mvc
            .updateTitle(request)
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.jsonPath("title").value(request.title))
    }

    @Test
    @DisplayName("미래의 나가 없는 사용자의 제목 등록/변경 테스트")
    fun given_userHasNotFutureMe_when_updateTitle_then_expectStatusCode404_and_responseExceptionResponse() {
        val request = UpdateFutureMeTitleRequest("테스트")

        mvc
            .updateTitle(request)
            .andExpect(MockMvcResultMatchers.status().isNotFound)
            .expectExceptionResponse()
    }

    @Test
    @DisplayName("미래의 나 길이 초과된 제목으로 등록/변경 테스트")
    fun given_requestWithExceedMaxLength_when_updateTitle_then_expectStatusCode400_and_responseBindingExceptionResponse() {
        val request = UpdateFutureMeTitleRequest("미래의 나 길이 초과된 제목 테스트")

        mvc.updateTitle(request)
            .expectBindingException()
    }

    @Test
    @DisplayName("미래의 나 캐릭터 변경")
    fun given_request_when_updateCharacter_then_responseUpdatedFutureMe() {
        val request = FutureMeCharacterRequest(Character.Type.CHEESE)
        mvc.createFutureMe(request)
        mvc.updateCharacter(request)
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.jsonPath("character.type").value(request.type.name))
    }

    @Test
    @DisplayName("미래의 나가 없는 사용자의 캐릭터 변경 테스트")
    fun given_userHasNotFutureMe_when_updateCharacter_then_expectStatusCode404_and_responseExceptionResponse() {
        val request = FutureMeCharacterRequest(Character.Type.CHEESE)

        mvc.updateCharacter(request)
            .andExpect(MockMvcResultMatchers.status().isNotFound)
            .expectExceptionResponse()
    }

    @Test
    @DisplayName("이미 미래의 나가 존재할 때 미래의 나 생성 테스트")
    fun given_requestWithAlreadyExistFutureMe_when_createFutureMe_then_expectStatusCode409_and_responseExceptionResponse(){
        val request = FutureMeCharacterRequest(Character.Type.CHEESE)
        mvc.createFutureMe(request)
        mvc.createFutureMe(request)
            .andExpect(MockMvcResultMatchers.status().isConflict)
            .expectExceptionResponse()
    }
}
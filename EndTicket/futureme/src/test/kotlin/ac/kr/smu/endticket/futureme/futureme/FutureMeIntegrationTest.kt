package ac.kr.smu.endticket.futureme.futureme

import ac.kr.smu.endticket.common.web.aop.BindExceptionAdvice
import ac.kr.smu.endticket.common.web.enum.CharacterType
import ac.kr.smu.endticket.common.web.test.andReturn
import ac.kr.smu.endticket.common.web.test.expectBindException
import ac.kr.smu.endticket.common.web.test.expectExceptionResponse
import ac.kr.smu.endticket.futureme.domain.converter.CharacterTypeConverter
import ac.kr.smu.endticket.futureme.domain.futureme.model.Character
import ac.kr.smu.endticket.futureme.domain.futureme.model.FutureMe
import ac.kr.smu.endticket.futureme.domain.futureme.repository.FutureMeRepository
import ac.kr.smu.endticket.futureme.service.FutureMeService
import ac.kr.smu.endticket.futureme.ui.controller.FutureMeController
import ac.kr.smu.endticket.futureme.ui.request.CreateFutureMeRequest
import ac.kr.smu.endticket.futureme.ui.request.UpdateFutureMeRequest
import ac.kr.smu.endticket.futureme.ui.response.FutureMeResponse
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration
import org.springframework.boot.autoconfigure.transaction.TransactionAutoConfiguration
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.format.support.FormattingConversionService
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.test.web.servlet.setup.MockMvcBuilders
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
@EnableJpaRepositories("ac.kr.smu.endticket.futureme.domain.futureme.repository")
@EntityScan("ac.kr.smu.endticket.futureMe.domain.futureMe.model")
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
            .createFutureMe(CreateFutureMeRequest(CharacterType.CHEESE))
            .andReturn<FutureMe>()

        mvc.findFutureMe()
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.content().string(ObjectMapper().writeValueAsString(FutureMeResponse.from(futureMe))))
    }

    @Test
    @DisplayName("존재하지 않는 미래의 나 조회 테스트")
    fun given_userDoesNotHasFutureMe_when_findFutureMe_then_responseExceptionResponseWithStatus404() {
        mvc.findFutureMe()
            .andExpect(MockMvcResultMatchers.status().isNotFound)
            .expectExceptionResponse()
    }

    @Test
    @DisplayName("캐릭터 이미지 조회 테스트")
    fun given_type_when_findCharacterImage_then_responseCharacterImage() {
        val type = CharacterType.CHEESE

        mvc.findCharacterImage(type)
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.valueOf("image/svg+xml")))
            .andExpect(MockMvcResultMatchers.content().bytes(type.imageResource.contentAsByteArray))
    }

    @Test
    @DisplayName("존재하지 않는 캐릭터 이미지 조회 테스트")
    fun given_notExistType_when_findCharacterImage_then_responseExceptionResponseWithStatus404() {
        mvc.perform(
            MockMvcRequestBuilders.get("$BASE_URL/characters/xxx")
        )
            .andExpect(MockMvcResultMatchers.status().isBadRequest)
            .expectExceptionResponse()
    }

    @Test
    @DisplayName("미래의 나 생성 테스트")
    fun given_type_when_create_then_responseCreatedFutureMe() {
        val request = CreateFutureMeRequest(CharacterType.CHEESE)

        mvc
            .createFutureMe(request)
            .andExpect(MockMvcResultMatchers.status().isCreated)
            .andExpect(MockMvcResultMatchers.jsonPath("character.type").value(request.type.name))
    }

    @Test
    @DisplayName("미래의 나 수정 테스트")
    fun given_request_when_update_then_responseUpdatedFutureMe() {
        mvc.createFutureMe(CreateFutureMeRequest(CharacterType.CHEESE)).andReturn<FutureMe>()

        mvc
            .updateFutureMe(UPDATE_REQUEST)
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.jsonPath("title").value(UPDATE_REQUEST.title))
            .andExpect(MockMvcResultMatchers.jsonPath("character.type").value(UPDATE_REQUEST.characterType?.name))
    }

    @Test
    @DisplayName("미래의 나가 없는 사용자의 수정 테스트")
    fun given_userHasNotFutureMe_when_update_then_responseExceptionResponseWithStatus404() =
        mvc
            .updateFutureMe(UPDATE_REQUEST)
            .andExpect(MockMvcResultMatchers.status().isNotFound)
            .expectExceptionResponse()


    @Test
    @DisplayName("미래의 나 길이 초과된 제목으로 등록/변경 테스트")
    fun given_requestWithExceedMaxLength_when_updateTitle_then_responseBindingExceptionResponseWithStatus400() {
        val request = UpdateFutureMeRequest("미래의 나 길이 초과된 제목 테스트")

        mvc.updateFutureMe(request)
            .expectBindException()
    }

    @Test
    @DisplayName("이미 미래의 나가 존재할 때 미래의 나 생성 테스트")
    fun given_requestWithAlreadyExistFutureMe_when_createFutureMe_then_responseExceptionResponseWithStatus409(){
        val request = CreateFutureMeRequest(CharacterType.CHEESE)
        mvc.createFutureMe(request)
        mvc.createFutureMe(request)
            .andExpect(MockMvcResultMatchers.status().isConflict)
            .expectExceptionResponse()
    }
}
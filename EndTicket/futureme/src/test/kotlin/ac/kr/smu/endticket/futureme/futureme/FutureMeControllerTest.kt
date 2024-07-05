package ac.kr.smu.endticket.futureme.futureme

import ac.kr.smu.endticket.common.web.aop.BindExceptionAdvice
import ac.kr.smu.endticket.common.web.enum.CharacterType
import ac.kr.smu.endticket.common.web.test.expectBindException
import ac.kr.smu.endticket.common.web.test.expectExceptionResponse
import ac.kr.smu.endticket.futureme.domain.converter.CharacterTypeConverter
import ac.kr.smu.endticket.futureme.domain.futureme.exception.FutureMeNotFoundException
import ac.kr.smu.endticket.futureme.domain.futureme.model.FutureMe
import ac.kr.smu.endticket.futureme.service.FutureMeService
import ac.kr.smu.endticket.futureme.ui.controller.FutureMeController
import ac.kr.smu.endticket.futureme.ui.request.CreateFutureMeRequest
import ac.kr.smu.endticket.futureme.ui.request.UpdateFutureMeRequest
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
class FutureMeControllerTest
    @Autowired
    constructor(
        @MockBean
        private val service: FutureMeService,
        controller: FutureMeController,
    ) {
        private val mvc =
            MockMvcBuilders
                .standaloneSetup(controller)
                .setControllerAdvice(BindExceptionAdvice())
                .setConversionService(FormattingConversionService().also { it.addConverter(CharacterTypeConverter()) })
                .build()

        @Test
        @DisplayName("미래의 나 조회 테스트")
        fun given_user_when_findFutureMe_then_responseFutureMe() {
            val futureMe = FutureMe.from(CreateFutureMeRequest(CharacterType.CHEESE), FutureMeTestParameters.USER_ID)

            Mockito
                .`when`(service.findFutureMe(FutureMeTestParameters.USER_ID))
                .thenReturn(futureMe.toResponse())

            mvc
                .findFutureMe()
                .andExpect(MockMvcResultMatchers.status().isOk)
                .andExpect(
                    MockMvcResultMatchers
                        .content()
                        .string(
                            ObjectMapper()
                                .writeValueAsString(futureMe.toResponse()),
                        ),
                )
        }

        @Test
        @DisplayName("존재하지 않는 미래의 나 조회 테스트")
        fun given_userDoesNotHaveFutureMe_when_findFutureMe_then_responseExceptionResponseWithStatus404() {
            Mockito
                .`when`(service.findFutureMe(FutureMeTestParameters.USER_ID))
                .thenAnswer { throw FutureMeNotFoundException(FutureMeTestParameters.USER_ID) }

            mvc
                .findFutureMe()
                .andExpect(MockMvcResultMatchers.status().isNotFound)
                .expectExceptionResponse()
        }

        @Test
        @DisplayName("캐릭터 이미지 조회 테스트")
        fun given_type_when_findCharacterImage_then_responseCharacterImage() {
            val type = CharacterType.CHEESE

            mvc
                .findCharacterImage(type)
                .andExpect(MockMvcResultMatchers.status().isOk)
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.valueOf("image/svg+xml")))
                .andExpect(MockMvcResultMatchers.content().bytes(type.imageResource.contentAsByteArray))
        }

        @Test
        @DisplayName("존재하지 않는 캐릭터 이미지 조회 테스트")
        fun given_nonExistentType_when_findCharacterImage_then_responseExceptionResponseWithStatus404() {
            mvc
                .perform(
                    MockMvcRequestBuilders.get("$BASE_URL/characters/xxx"),
                ).andExpect(MockMvcResultMatchers.status().isBadRequest)
                .expectExceptionResponse()
        }

        @Test
        @DisplayName("미래의 나 생성 테스트")
        fun given_type_when_createFutureMe_then_responseCreatedFutureMe() {
            val request = CreateFutureMeRequest(CharacterType.CHEESE)

            Mockito
                .`when`(service.createFutureMe(request, FutureMeTestParameters.USER_ID))
                .thenReturn(FutureMe.from(request, FutureMeTestParameters.USER_ID).toResponse())

            mvc
                .createFutureMe(request)
                .andExpect(MockMvcResultMatchers.status().isCreated)
                .andExpect(MockMvcResultMatchers.jsonPath("character.type").value(request.type.name))
        }

        @Test
        @DisplayName("미래의 나 제목 수정 테스트")
        fun given_request_when_updateFutureMe_then_responseUpdatedFutureMe() {
            val futureMe = FutureMe.from(CreateFutureMeRequest(CharacterType.CHEESE), FutureMeTestParameters.USER_ID)

            Mockito
                .`when`(service.updateFutureMe(FutureMeTestParameters.UPDATE_REQUEST, FutureMeTestParameters.USER_ID))
                .thenReturn(futureMe.also { it.update(FutureMeTestParameters.UPDATE_REQUEST) }.toResponse())

            mvc
                .updateFutureMe(FutureMeTestParameters.UPDATE_REQUEST)
                .andExpect(MockMvcResultMatchers.status().isOk)
                .andExpect(MockMvcResultMatchers.jsonPath("title").value(FutureMeTestParameters.UPDATE_REQUEST.title))
        }

        @Test
        @DisplayName("미래의 나가 없는 사용자의 수정 테스트")
        fun given_userDoesNotHaveFutureMee_when_updateFutureMe_then_responseExceptionResponseWithStatus404() {
            val request = UpdateFutureMeRequest("테스트")

            Mockito
                .`when`(service.updateFutureMe(request, FutureMeTestParameters.USER_ID))
                .thenAnswer { throw FutureMeNotFoundException(FutureMeTestParameters.USER_ID) }

            mvc
                .updateFutureMe(request)
                .andExpect(MockMvcResultMatchers.status().isNotFound)
                .expectExceptionResponse()
        }

        @Test
        @DisplayName("미래의 나 길이 초과된 제목으로 등록/변경 테스트")
        fun given_requestWithExceedMaxLength_when_updateFutureMe_then_responseBindExceptionResponseWithStatus400() {
            val request = UpdateFutureMeRequest("미래의 나 길이 초과된 제목 테스트")

            mvc
                .updateFutureMe(request)
                .expectBindException()
        }

        @Test
        @DisplayName("이미 미래의 나가 존재할 때 미래의 나 생성 테스트")
        fun given_requestWithAlreadyExistFutureMe_when_createFutureMe_then_responseExceptionResponseWithStatus409() {
            val request = CreateFutureMeRequest(CharacterType.CHEESE)

            Mockito
                .`when`(service.createFutureMe(request, FutureMeTestParameters.USER_ID))
                .thenAnswer { throw IllegalStateException("") }

            mvc
                .createFutureMe(request)
                .andExpect(MockMvcResultMatchers.status().isConflict)
                .expectExceptionResponse()
        }
    }

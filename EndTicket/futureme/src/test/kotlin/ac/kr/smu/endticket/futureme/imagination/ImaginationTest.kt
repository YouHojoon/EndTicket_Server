package ac.kr.smu.endticket.futureme.imagination

import ac.kr.smu.endticket.common.web.enum.Color
import ac.kr.smu.endticket.futureme.domain.imagination.exception.ImaginationOwnershipException
import ac.kr.smu.endticket.futureme.domain.imagination.model.Imagination
import ac.kr.smu.endticket.futureme.ui.request.ImaginationRequest
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import kotlin.test.assertEquals

class ImaginationTest {
    @Test
    @DisplayName("상상해보기 생성 테스트")
    fun given_request_when_from_then_returnCreatedImagination(){
        val imagination = Imagination.from(REQUEST, USER_ID)
        val response = imagination.toResponse()

        assertEquals(response.behavior, REQUEST.behavior)
        assertEquals(response.target, REQUEST.target)
        assertEquals(response.color, REQUEST.color)
    }

    @DisplayName("상상해보기 수정 테스트")
    @ParameterizedTest
    @MethodSource("${ImaginationParameters.PATH}#provideImaginationAndUpdateRequest")
    fun given_requestAndUserId_when_update_then_success(imagination: Imagination, request: ImaginationRequest){
        imagination.update(request, imagination.userId)

        val response = imagination.toResponse()
        assertEquals(request.behavior, response.behavior)
        assertEquals(request.target, response.target)
        assertEquals(request.color, response.color)
    }

    @DisplayName("소유자가 아닌 사용자 상상해보기 수정 테스트")
    @ParameterizedTest
    @MethodSource("${ImaginationParameters.PATH}#provideImaginationAndUpdateRequest")
    fun given_userIdWhoNotOwnerAndRequest_when_update_then_throwImaginationOwnershipException(imagination: Imagination, request: ImaginationRequest){
        assertThrows<ImaginationOwnershipException> {  imagination.update(request, 2L)}
    }

    @DisplayName("상상해보기 완료 테스트")
    @ParameterizedTest
    @MethodSource("a${ImaginationParameters.PATH}s#provideImagination")
    fun given_userId_when_complete_then_success(imagination: Imagination){
        assertDoesNotThrow { imagination.complete(imagination.userId)}
    }

    @DisplayName("소유자가 아닌 사용자 완료 테스트")
    @ParameterizedTest
    @MethodSource("${ImaginationParameters.PATH}#provideImagination")
    fun given_userIdWhoNotOwner_when_complete_then_throwImaginationOwnershipException(imagination: Imagination){
        assertThrows<ImaginationOwnershipException> {  imagination.complete(2L)}
    }

}
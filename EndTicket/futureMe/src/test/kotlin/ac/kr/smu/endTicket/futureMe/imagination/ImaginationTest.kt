package ac.kr.smu.endTicket.futureMe.imagination

import ac.kr.smu.endTicket.futureMe.domain.imagination.exception.ImaginationOwnershipException
import ac.kr.smu.endTicket.futureMe.domain.imagination.model.Imagination
import ac.kr.smu.endTicket.futureMe.ui.request.ImaginationRequest
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals

class ImaginationTest {

    @Test
    @DisplayName("상상해보기 생성 테스트")
    fun given_imaginationRequest_when_from_then_returnCreatedImagination(){
        val imagination = Imagination.from(request, USER_ID)

        assertEquals(imagination.behavior, request.behavior)
        assertEquals(imagination.target, request.target)
        assertEquals(imagination.color, request.color)
    }

    @Test
    @DisplayName("상상해보기 수정 테스트")
    fun given_imaginationRequest_when_update_then_success(){
        val imagination = Imagination.from(request, USER_ID)
        val updateRequest = ImaginationRequest("aa","www",Imagination.Color.GRAY2)

        imagination.update(updateRequest, USER_ID)

        assertEquals(updateRequest.behavior, imagination.behavior)
        assertEquals(updateRequest.target, imagination.target)
        assertEquals(updateRequest.color, imagination.color)
    }

    @Test
    @DisplayName("소유자가 아닌 사용자 상상해보기 수정 테스트")
    fun given_userWhoNotOwner_when_update_then_throwImaginationOwnershipException(){
        val imagination = Imagination.from(request, USER_ID)
        val updateRequest = ImaginationRequest("aa","www",Imagination.Color.GRAY2)

        assertThrows<ImaginationOwnershipException> {  imagination.update(updateRequest, 2L)}
    }

    @Test
    @DisplayName("상상해보기 완료 테스트")
    fun given_user_when_complete_then_success(){
        val imagination = Imagination.from(request, USER_ID)
        assertDoesNotThrow { imagination.complete(USER_ID)}
    }

    @Test
    @DisplayName("소유자가 아닌 사용자 완료 테스트")
    fun given_userWhoNotOwner_when_complete_then_throwImaginationOwnershipException(){
        val imagination = Imagination.from(request, USER_ID)

        assertThrows<ImaginationOwnershipException> {  imagination.complete(2L)}
    }

}
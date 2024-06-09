package ac.kr.smu.endTicket.futureMe.futureMe

import ac.kr.smu.endTicket.futureMe.domain.futureMe.model.Character
import ac.kr.smu.endTicket.futureMe.domain.futureMe.model.FutureMe
import ac.kr.smu.endTicket.futureMe.ui.request.FutureMeCharacterRequest
import ac.kr.smu.endTicket.futureMe.ui.request.UpdateFutureMeTitleRequest
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class FutureMeTest {
    @Test
    @DisplayName("미래의 나 제목 변경 테스트")
    fun given_updateTitleOfFutureMeRequest_when_updateTitle_then_updateTitle(){
        val futureMe = FutureMe.from(FutureMeCharacterRequest(Character.Type.CHEESE), USER_ID)
        val request = UpdateFutureMeTitleRequest("테스트")

        futureMe.updateTitle(request)

        assertEquals(request.title, futureMe.title)
    }

    @Test
    @DisplayName("캐릭터 설정 테스트")
    fun given_type_when_setCharacter_then_success(){
        val futureMe = FutureMe.from(FutureMeCharacterRequest(Character.Type.CHEESE), USER_ID)
        val request = FutureMeCharacterRequest(Character.Type.VEGA)
        futureMe.updateCharacter(request, USER_ID)

        val character = futureMe.character

        assertNotNull(character)
        assertEquals(request.type, character.type)
    }

    @Test
    @DisplayName("소유자가 아닌 사용자의 캐릭터 설정 테스트")
    fun given_userWhoNotOwner_setCharacter_then_throwIllegalException(){
        val futureMe = FutureMe.from(FutureMeCharacterRequest(Character.Type.CHEESE), USER_ID)
        val request = FutureMeCharacterRequest(Character.Type.VEGA)

        assertThrows<IllegalStateException> {
            futureMe.updateCharacter(request, 2L)
        }

    }
}
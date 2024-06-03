package ac.kr.smu.endTicket.futureMe

import ac.kr.smu.endTicket.futureMe.domain.futureMe.model.Character
import ac.kr.smu.endTicket.futureMe.domain.futureMe.model.FutureMe
import ac.kr.smu.endTicket.futureMe.ui.request.UpdateTitleOfFutureMeRequest
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class FutureMeTest {
    @Test
    @DisplayName("미래의 나 제목 변경 테스트")
    fun given_updateTitleOfFutureMeRequest_when_updateTitle_then_updateTitle(){
        val futureMe = FutureMe(USER_ID, Character.Type.CHEESE)
        val request = UpdateTitleOfFutureMeRequest("테스트")

        futureMe.updateTitle(request)

        assertEquals(request.title, futureMe.title)
    }

    @Test
    @DisplayName("캐릭터 설정 테스트")
    fun given_type_when_setCharacter_then_success(){
        val futureMe = FutureMe(USER_ID, Character.Type.CHEESE)
        val type = Character.Type.VEGA
        futureMe.setCharacter(type, USER_ID)

        val character = futureMe.character

        assertNotNull(character)
        assertEquals(type, character.type)
    }

    @Test
    @DisplayName("소유자가 아닌 사용자의 캐릭터 설정 테스트")
    fun given_userWhoNotOwner_setCharacter_then_throwIllegalException(){
        val futureMe = FutureMe(USER_ID, Character.Type.CHEESE)
        val type = Character.Type.VEGA

        assertThrows<IllegalStateException> {
            futureMe.setCharacter(type, 2L)
        }

    }
}
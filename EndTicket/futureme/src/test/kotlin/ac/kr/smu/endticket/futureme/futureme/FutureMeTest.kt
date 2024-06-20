package ac.kr.smu.endticket.futureme.futureme

import ac.kr.smu.endticket.common.web.enum.CharacterType
import ac.kr.smu.endticket.futureme.domain.futureme.model.Character
import ac.kr.smu.endticket.futureme.domain.futureme.model.FutureMe
import ac.kr.smu.endticket.futureme.ui.request.CreateFutureMeRequest
import ac.kr.smu.endticket.futureme.ui.request.UpdateFutureMeRequest
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class FutureMeTest {
    @Test
    @DisplayName("미래의 나 제목 변경 테스트")
    fun given_updateTitleOfFutureMeRequest_when_updateTitle_then_updateTitle(){
        val futureMe = FutureMe.from(CreateFutureMeRequest(CharacterType.CHEESE), USER_ID)
        val request = UpdateFutureMeRequest("테스트")

        futureMe.update(request)

        assertEquals(request.title, futureMe.title)
    }

    @Test
    @DisplayName("캐릭터 설정 테스트")
    fun given_type_when_setCharacter_then_success(){
        val futureMe = FutureMe.from(CreateFutureMeRequest(CharacterType.CHEESE), USER_ID)
        futureMe.update(UPDATE_REQUEST)

        val character = futureMe.character

        assertNotNull(character)
        assertEquals(UPDATE_REQUEST.characterType, character.type)
    }
}
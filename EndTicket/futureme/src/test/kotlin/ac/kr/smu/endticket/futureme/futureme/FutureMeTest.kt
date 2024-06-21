package ac.kr.smu.endticket.futureme.futureme

import ac.kr.smu.endticket.common.web.enum.CharacterType
import ac.kr.smu.endticket.futureme.domain.futureme.model.Character
import ac.kr.smu.endticket.futureme.domain.futureme.model.FutureMe
import ac.kr.smu.endticket.futureme.ui.request.CreateFutureMeRequest
import ac.kr.smu.endticket.futureme.ui.request.UpdateFutureMeRequest
import org.hibernate.sql.Update
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class FutureMeTest {
    @Test
    @DisplayName("미래의 나 수정 테스트")
    fun given_updateTitleOfFutureMeRequest_when_update_then_success(){
        val futureMe = FutureMe.from(CreateFutureMeRequest(CharacterType.CHEESE), USER_ID)

        futureMe.update(UPDATE_REQUEST)

        val characterType = UPDATE_REQUEST.characterType
        assertNotNull(characterType)
        assertEquals(UPDATE_REQUEST.title, futureMe.title)
        assertEquals(characterType, futureMe.character.type)
    }
}
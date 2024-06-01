package ac.kr.smu.endTicket.futureMe

import ac.kr.smu.endTicket.futureMe.domain.model.Character
import ac.kr.smu.endTicket.futureMe.domain.model.event.FutureMeCompletionEvent
import ac.kr.smu.endTicket.futureMe.domain.model.event.TicketCompletionEvent
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CharacterTest {
    @Test
    @DisplayName("캐릭터 각 타입별 이미지가 존재하는 지 테스트")
    fun test_eachTypeImageIsExist(){
        assertTrue(Character.Type.values().map { it.image.exists() }.none { !it })
    }

    @Test
    @DisplayName("각 이벤트 별 경험치 상승 테스트")
    fun given_event_when_gainExperiencePoints_then_increasedExperiencePoints(){
        val character = Character(Character.Type.CHEESE)

        character.gainExperiencePoints(Mockito.mock<TicketCompletionEvent>())
        character.gainExperiencePoints(Mockito.mock<FutureMeCompletionEvent>())

        assertEquals(30, character.experiencePoints)
    }
}
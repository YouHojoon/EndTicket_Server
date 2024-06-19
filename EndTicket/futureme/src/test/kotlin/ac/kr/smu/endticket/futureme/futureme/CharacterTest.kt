package ac.kr.smu.endticket.futureme.futureme

import ac.kr.smu.endticket.futureme.domain.event.model.ImaginationCompletedEvent
import ac.kr.smu.endticket.futureme.domain.event.model.TicketCompletedEvent
import ac.kr.smu.endticket.futureme.domain.futureme.model.Character
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CharacterTest {
    @Test
    @DisplayName("캐릭터 각 타입별 이미지가 존재하는 지 테스트")
    fun test_eachTypeImageIsExist(){
        assertTrue(Character.Type.values().map { it.imageResource.exists() }.none { !it })
    }

    @Test
    @DisplayName("각 이벤트 별 경험치 상승 테스트")
    fun given_event_when_gainExperiencePoints_then_increasedExperiencePoints(){
        val character = Character(Character.Type.CHEESE)

        character.gainExperiencePoints(Mockito.mock<TicketCompletedEvent>())
        character.gainExperiencePoints(Mockito.mock<ImaginationCompletedEvent>())

        assertEquals(30, character.experiencePoints)
    }

    @Test
    @DisplayName("레벨 업 테스트")
    fun given_characterWithReachedMaxExperiencePoints_when_gainExperiencePoints_then_increasedLevel(){
        val character = Character(Character.Type.CHEESE)
        val beforeLevel = character.level

        repeat(5){
            character.gainExperiencePoints(Mockito.mock<TicketCompletedEvent>())
        }

        assertEquals(beforeLevel + 1, character.level)
        assertEquals(0, character.experiencePoints)
    }

    @Test
    @DisplayName("레벨 및 경험치 최대치 테스트")
    fun given_characterWithReachedMaxLevel_when_gainExperiencePoints_then_nothingChange(){
        val character = Character(Character.Type.CHEESE)
        repeat(40 * 1000 / 20){
            character.gainExperiencePoints(Mockito.mock<TicketCompletedEvent>())
        }
        assertEquals(40, character.level)
        assertEquals(100, character.experiencePoints)
    }
}
package ac.kr.smu.endTicket.futureMe

import ac.kr.smu.endTicket.futureMe.domain.Character
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import kotlin.test.assertTrue

class CharacterTest {
    @Test
    @DisplayName("캐릭터 각 타입별 이미지가 존재하는 지 테스트")
    fun test_eachTypeImageIsExist(){
        assertTrue(Character.Type.values().map { it.image.exists() }.none { !it })
    }
}
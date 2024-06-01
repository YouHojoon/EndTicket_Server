package ac.kr.smu.endTicket.futureMe

import ac.kr.smu.endTicket.futureMe.domain.model.FutureMe
import ac.kr.smu.endTicket.futureMe.domain.model.Imagination
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class ImaginationTest {

    @Test
    @DisplayName("상상해보기 생성 테스트")
    fun given_imaginationRequest_when_from_then_returnCreatedImagination(){
        val imagination = Imagination.from(request, FutureMe(USER_ID))

        assertEquals(imagination.behavior, request.behavior)
        assertEquals(imagination.target, request.target)
        assertEquals(imagination.color, request.color)
    }
}
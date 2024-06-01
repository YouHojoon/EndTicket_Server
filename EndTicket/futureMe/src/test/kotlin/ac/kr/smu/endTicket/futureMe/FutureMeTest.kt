package ac.kr.smu.endTicket.futureMe

import ac.kr.smu.endTicket.futureMe.domain.FutureMe
import ac.kr.smu.endTicket.futureMe.ui.request.UpdateTitleOfFutureMeRequest
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class FutureMeTest {
    @Test
    @DisplayName("미래의 나 제목 변경 테스트")
    fun given_updateTitleOfFutureMeRequest_when_updateTitle_then_updateTitle(){
        val futureMe = FutureMe(USER_ID)
        val request = UpdateTitleOfFutureMeRequest("테스트")

        futureMe.updateTitle(request)

        assertEquals(request.title, futureMe.title)
    }
}
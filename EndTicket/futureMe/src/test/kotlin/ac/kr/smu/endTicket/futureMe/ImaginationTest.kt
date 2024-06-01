package ac.kr.smu.endTicket.futureMe

import ac.kr.smu.endTicket.futureMe.domain.model.FutureMe
import ac.kr.smu.endTicket.futureMe.domain.imagination.model.Imagination
import ac.kr.smu.endTicket.futureMe.ui.request.ImaginationRequest
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
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
}
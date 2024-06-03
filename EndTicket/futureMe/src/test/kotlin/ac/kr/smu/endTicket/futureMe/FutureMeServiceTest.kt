package ac.kr.smu.endTicket.futureMe

import ac.kr.smu.endTicket.futureMe.domain.futureMe.model.Character
import ac.kr.smu.endTicket.futureMe.domain.futureMe.model.FutureMe
import ac.kr.smu.endTicket.futureMe.domain.futureMe.repository.FutureMeRepository
import ac.kr.smu.endTicket.futureMe.service.FutureMeService
import ac.kr.smu.endTicket.futureMe.ui.request.UpdateTitleOfFutureMeRequest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations
import org.mockito.junit.jupiter.MockitoExtension
import java.util.*
import kotlin.test.assertEquals


@ExtendWith(MockitoExtension::class)
class FutureMeServiceTest(
    @Mock
    private val repo: FutureMeRepository,

) {
    @InjectMocks
    private lateinit var service: FutureMeService

    @BeforeEach
    fun init(){
        MockitoAnnotations.openMocks(this)
    }

    @Test
    @DisplayName("제목 수정 테스트")
    fun given_updateTitleOfFutureMeRequest_when_updateTitle_then_returnUpdatedFutureMe(){
        val futureMe = FutureMe(USER_ID, Character.Type.CHEESE)
        val title = "테스트"
        Mockito.`when`(repo.findById(USER_ID))
            .thenReturn(Optional.of(futureMe))

        val updated = service.updateTitle(UpdateTitleOfFutureMeRequest(title), USER_ID)

        assertEquals(title, updated.title)
    }

}
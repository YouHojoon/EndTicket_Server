package ac.kr.smu.endTicket.futureMe

import ac.kr.smu.endTicket.futureMe.domain.imagination.exception.NotFoundImaginationException
import ac.kr.smu.endTicket.futureMe.domain.imagination.model.Imagination
import ac.kr.smu.endTicket.futureMe.domain.imagination.repository.ImaginationRepository
import ac.kr.smu.endTicket.futureMe.service.ImaginationService
import ac.kr.smu.endTicket.futureMe.ui.request.ImaginationRequest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations
import org.mockito.junit.jupiter.MockitoExtension
import java.util.*
import javax.swing.text.html.Option
import kotlin.test.assertEquals

@ExtendWith(MockitoExtension::class)
class ImaginationServiceTest(
    @Mock
    private val repo: ImaginationRepository
) {
    @InjectMocks
    private lateinit var service: ImaginationService

    @BeforeEach
    fun init() {
        MockitoAnnotations.openMocks(this)
    }

    @Test
    @DisplayName("제한 개수 이상으로 상상해보기 생성 테스트")
    fun given_requestMoreThanImaginationLimit_when_createImagination_then_throwIllegalStateException(){
        Mockito.`when`(repo.countById(USER_ID))
            .thenReturn(6)

        assertThrows<IllegalStateException> {  service.createImagination(request, USER_ID)}
    }
    @Test
    @DisplayName("상상해보기 수정 테스트")
    fun given_request_when_updateImagination_then_returnUpdatedImagination(){
        val imagination = Imagination.from(request, USER_ID)
        val request = ImaginationRequest("xx", "zzz", Imagination.Color.GRAY2)
        Mockito
            .`when`(repo.findById(imagination.id))
            .thenReturn(Optional.of(imagination))

        val updated = service.updateImagination(request, imagination.id, USER_ID)

        assertEquals(request.behavior, updated.behavior)
        assertEquals(request.target, updated.target)
        assertEquals(request.color, updated.color)
    }

    @Test
    @DisplayName("소유자가 아닌 사용자의 상상해보기 테스트")
    fun given_userWhoNotOwner_when_updateImagination_then_throwIllegalStateException(){
        val imagination = Imagination.from(request, USER_ID)

        Mockito.`when`(repo.findById(imagination.id))
            .thenReturn(Optional.of(imagination))

        val request = ImaginationRequest("xx", "zzz", Imagination.Color.GRAY2)
        assertThrows<IllegalStateException> {  service.updateImagination(request, imagination.id, 2L)}
    }

    @Test
    @DisplayName("존재하지 않는 상상해보기 수정 테스트")
    fun given_notExistImagination_when_updateImagination_then_throwNotFoundImaginationException(){
        Mockito.`when`(repo.findById(Mockito.anyLong()))
            .thenReturn(Optional.empty())

        val request = ImaginationRequest("xx", "zzz", Imagination.Color.GRAY2)
        assertThrows<NotFoundImaginationException> {  service.updateImagination(request,1L, USER_ID)}
    }
}
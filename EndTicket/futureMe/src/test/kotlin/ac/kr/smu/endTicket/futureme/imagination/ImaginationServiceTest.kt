package ac.kr.smu.endTicket.futureme.imagination

import ac.kr.smu.endTicket.futureme.domain.imagination.exception.ImaginationNotFoundException
import ac.kr.smu.endTicket.futureme.domain.imagination.exception.ImaginationOwnershipException
import ac.kr.smu.endTicket.futureme.domain.imagination.model.Imagination
import ac.kr.smu.endTicket.futureme.domain.imagination.repository.ImaginationRepository
import ac.kr.smu.endTicket.futureme.service.FutureMeEventService
import ac.kr.smu.endTicket.futureme.service.ImaginationService
import ac.kr.smu.endTicket.futureme.ui.request.ImaginationRequest
import ac.kr.smu.endTicket.futureme.ui.response.ImaginationResponse
import ac.kr.smu.endTicket.test.mockAny
import org.junit.jupiter.api.*
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations
import org.mockito.junit.jupiter.MockitoExtension
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@ExtendWith(MockitoExtension::class)
class ImaginationServiceTest(
    @Mock
    private val repo: ImaginationRepository,
    @Mock
    private val eventService: FutureMeEventService
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
        Mockito.`when`(repo.countByUserIdAndIsCompleteIsFalse(USER_ID))
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
    fun given_userWhoNotOwner_when_updateImagination_then_throwImaginationOwnershipException(){
        val imagination = Imagination.from(request, USER_ID)

        Mockito.`when`(repo.findById(imagination.id))
            .thenReturn(Optional.of(imagination))

        val request = ImaginationRequest("xx", "zzz", Imagination.Color.GRAY2)
        assertThrows<ImaginationOwnershipException> {  service.updateImagination(request, imagination.id, 2L)}
    }

    @Test
    @DisplayName("존재하지 않는 상상해보기 수정 테스트")
    fun given_notExistImagination_when_updateImagination_then_throwImaginationNotFoundIException(){
        Mockito.`when`(repo.findById(Mockito.anyLong()))
            .thenReturn(Optional.empty())

        val request = ImaginationRequest("xx", "zzz", Imagination.Color.GRAY2)
        assertThrows<ImaginationNotFoundException> {  service.updateImagination(request,1L, USER_ID)}
    }

    @Test
    @DisplayName("상상해보기 완료 테스트")
    fun given_imagination_when_completeImagination_then_publishImaginationCompletionEvent(){
        val imagination = Imagination.from(request, USER_ID)

        Mockito.`when`(repo.findById(imagination.id))
            .thenReturn(Optional.of(imagination))

        service.completeImagination(imagination.id, USER_ID)

        Mockito.verify(eventService, Mockito.times(1)).publishEvent(mockAny())
    }

    @Test
    @DisplayName("존재하지 않는 상상해보기 완료 테스트")
    fun given_notExistImagination__when_completeImagination_then_throwNotFoundImagination(){
        Mockito.`when`(repo.findById(mockAny()))
            .thenReturn(Optional.empty())

        assertThrows<ImaginationNotFoundException> {
            service.completeImagination(1L, USER_ID)
        }
    }

    @Test
    @DisplayName("소유자가 아닌 사용자의 상상해보기 완료 테스트")
    fun given_userWhoNotOwner__when_completeImagination_then_throwImaginationOwnershipException(){
        val imagination = Imagination.from(request, USER_ID)

        Mockito.`when`(repo.findById(imagination.id))
            .thenReturn(Optional.of(imagination))

        assertThrows<ImaginationOwnershipException> {
            service.completeImagination(imagination.id, 2L)
        }
    }

    @Test
    @DisplayName("상상해보기 조회 테스트")
    fun given_user_when_findImagination_then_return_imaginations(){
        val imaginations = setOf(Imagination.from(request, USER_ID))

        Mockito.`when`(repo.findByUserIdAndIsCompleteIsFalse(USER_ID))
            .thenReturn(imaginations)

        val result = service.findImaginations(USER_ID)
        assertTrue(result.isNotEmpty())

        for((lhs,rhs) in imaginations.zip(result)){
            assertEquals(ImaginationResponse.from(lhs),rhs)
        }
    }

    @Test
    @DisplayName("상상해보기 삭제 테스트")
    fun given_id_when_deleteImagination_then_success(){
        val imagination = Imagination.from(request, USER_ID)

        Mockito.`when`(repo.findById(imagination.id))
            .thenReturn(Optional.of(imagination))

        assertDoesNotThrow { service.deleteImagination(imagination.id, USER_ID)}
        Mockito.verify(repo, Mockito.times(1)).delete(imagination)
    }

    @Test
    @DisplayName("존재하지 않는 상상해보기 삭제 테스트")
    fun given_notExistImagination_when_deleteImagination_then_throwImaginationNotFoundIException(){
        Mockito.`when`(repo.findById(Mockito.anyLong()))
            .thenReturn(Optional.empty())

        assertThrows<ImaginationNotFoundException> { service.deleteImagination(1L, USER_ID) }
    }
    @Test
    @DisplayName("소유자가 아닌 사용자의 상상해보기 삭제 테스트")
    fun given_userWhoNotOwner_when_deleteImagination_then_throwImaginationOwnershipException(){
        val imagination = Imagination.from(request, USER_ID)

        Mockito.`when`(repo.findById(imagination.id))
            .thenReturn(Optional.of(imagination))

        assertThrows<ImaginationOwnershipException> { service.deleteImagination(imagination.id, 2L) }
    }
}
package ac.kr.smu.endticket.futureme.imagination

import ac.kr.smu.endticket.common.test.mockAny
import ac.kr.smu.endticket.futureme.domain.imagination.model.Imagination
import ac.kr.smu.endticket.futureme.domain.imagination.repository.ImaginationRepository
import ac.kr.smu.endticket.futureme.service.FutureMeEventService
import ac.kr.smu.endticket.futureme.service.ImaginationService
import ac.kr.smu.endticket.futureme.ui.request.ImaginationRequest
import org.junit.jupiter.api.*
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations
import org.mockito.junit.jupiter.MockitoExtension
import java.util.*
import kotlin.reflect.KClass
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

@ExtendWith(MockitoExtension::class)
class ImaginationServiceTest(
    @Mock
    private val repo: ImaginationRepository,
    @Mock
    private val eventService: FutureMeEventService,
) {
    @InjectMocks
    private lateinit var service: ImaginationService

    @BeforeEach
    fun init() {
        MockitoAnnotations.openMocks(this)
    }

    @Test
    @DisplayName("제한 개수 이상으로 상상해보기 생성 테스트")
    fun given_requestMoreThanImaginationLimit_when_createImagination_then_throwIllegalStateException() {
        Mockito
            .`when`(repo.countByUserIdAndIsCompleteIsFalse(ImaginationParameters.USER_ID))
            .thenReturn(6)

        assertThrows<IllegalStateException> {
            service.createImagination(
                ImaginationParameters.REQUEST,
                ImaginationParameters.USER_ID,
            )
        }
    }

    @ParameterizedTest
    @DisplayName("상상해보기 수정 테스트")
    @MethodSource("${ImaginationParameters.PATH}#provideImaginationAndRequest")
    fun given_idAndRequest_when_updateImagination_then_returnUpdatedImagination(
        imagination: Imagination,
        request: ImaginationRequest,
    ) {
        Mockito
            .`when`(repo.findById(imagination.id))
            .thenReturn(Optional.of(imagination))

        val updated = service.updateImagination(request, imagination.id, imagination.userId)

        assertEquals(request.behavior, updated.behavior)
        assertEquals(request.target, updated.target)
        assertEquals(request.color, updated.color)
    }

    @ParameterizedTest
    @DisplayName("비정상적인 상상해보기 수정 테스트")
    @MethodSource("${ImaginationParameters.PATH}#provideInvalidImaginationAndReqeuest")
    fun given_invalidIdAndRequest_when_updateImagination_then_throwExpectedException(
        imagination: Imagination?,
        request: ImaginationRequest,
        userId: Long,
        e: KClass<out Throwable>,
    ) {
        val id = imagination?.id ?: 1L

        Mockito
            .`when`(repo.findById(id))
            .thenReturn(Optional.ofNullable(imagination))

        assertFailsWith(e) { service.updateImagination(request, id, userId) }
    }

    @ParameterizedTest
    @DisplayName("상상해보기 완료 테스트")
    @MethodSource("${ImaginationParameters.PATH}#provideImagination")
    fun given_id_when_completeImagination_then_publishImaginationCompletionEvent(imagination: Imagination) {
        Mockito
            .`when`(repo.findById(imagination.id))
            .thenReturn(Optional.of(imagination))

        service.completeImagination(imagination.id, imagination.userId)

        Mockito.verify(eventService, Mockito.times(1)).publish(mockAny())
    }

    @ParameterizedTest
    @DisplayName("비정상적인 상상해보기 완료 테스트")
    @MethodSource("${ImaginationParameters.PATH}#provideInvalidImagination")
    fun given_invalidId_when_completeImagination_then_throwExpectedException(
        imagination: Imagination?,
        userId: Long,
        e: KClass<out Throwable>,
    ) {
        val id = imagination?.id ?: 1L

        Mockito
            .`when`(repo.findById(id))
            .thenReturn(Optional.ofNullable(imagination))

        assertFailsWith(e) { service.completeImagination(id, userId) }
    }

    @ParameterizedTest
    @DisplayName("상상해보기 조회 테스트")
    @MethodSource("${ImaginationParameters.PATH}#provideImaginations")
    fun given_userId_when_findImagination_then_return_imaginations(imaginations: Set<Imagination>) {
        Mockito
            .`when`(repo.findByUserIdAndIsCompleteIsFalse(ImaginationParameters.USER_ID))
            .thenReturn(imaginations)

        val result = service.findImaginations(ImaginationParameters.USER_ID)
        assertTrue(result.isNotEmpty())

        for ((lhs, rhs) in imaginations.zip(result)) {
            assertEquals(lhs.toResponse(), rhs)
        }
    }

    @ParameterizedTest
    @DisplayName("상상해보기 삭제 테스트")
    @MethodSource("${ImaginationParameters.PATH}#provideImagination")
    fun given_id_when_deleteImagination_then_success(imagination: Imagination) {
        Mockito
            .`when`(repo.findById(imagination.id))
            .thenReturn(Optional.of(imagination))

        assertDoesNotThrow { service.deleteImagination(imagination.id, imagination.userId) }
        Mockito.verify(repo, Mockito.times(1)).delete(imagination)
    }

    @ParameterizedTest
    @DisplayName("비정상적인 상상해보기 삭제 테스트")
    @MethodSource("${ImaginationParameters.PATH}#provideInvalidImagination")
    fun given_invalidId_when_deleteImagination_then_throwExpectedException(
        imagination: Imagination?,
        userId: Long,
        e: KClass<out Throwable>,
    ) {
        val id = imagination?.id ?: 1L

        Mockito
            .`when`(repo.findById(id))
            .thenReturn(Optional.ofNullable(imagination))

        assertFailsWith(e) { service.deleteImagination(id, userId) }
    }

    @ParameterizedTest
    @DisplayName("사용자 id로 삭제 테스트")
    @MethodSource("${ImaginationParameters.PATH}#provideImagination")
    fun given_userId_when_deleteByUserId_then_deleteImaginationOfUser(imagination: Imagination) {
    }
}

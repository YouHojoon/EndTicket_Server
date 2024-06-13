package ac.kr.smu.endTicket.futureMe.futureMe

import ac.kr.smu.endTicket.futureMe.domain.event.model.Event
import ac.kr.smu.endTicket.futureMe.domain.event.model.ImaginationCompletionEvent
import ac.kr.smu.endTicket.futureMe.domain.event.model.TicketCompletionEvent
import ac.kr.smu.endTicket.futureMe.domain.futureMe.exception.NotFoundFutureMeException
import ac.kr.smu.endTicket.futureMe.domain.futureMe.model.Character
import ac.kr.smu.endTicket.futureMe.domain.futureMe.model.FutureMe
import ac.kr.smu.endTicket.futureMe.domain.futureMe.repository.FutureMeRepository
import ac.kr.smu.endTicket.futureMe.service.FutureMeService
import ac.kr.smu.endTicket.futureMe.ui.request.FutureMeCharacterRequest
import ac.kr.smu.endTicket.futureMe.ui.request.UpdateFutureMeTitleRequest
import ac.kr.smu.endTicket.test.mockAny
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
        val futureMe = FutureMe.from(FutureMeCharacterRequest(Character.Type.CHEESE), USER_ID)
        val title = "테스트"
        Mockito.`when`(repo.findById(USER_ID))
            .thenReturn(Optional.of(futureMe))

        val updated = service.updateTitle(UpdateFutureMeTitleRequest(title), USER_ID)

        assertEquals(title, updated.title)
    }

    @Test
    @DisplayName("존재하지 않는 미래의 나 수정 테스트")
    fun given_notExistFutureMe_when_updateTitle_then_throwNotFoundFutureMeException(){
        val title = "테스트"
        Mockito.`when`(repo.findById(USER_ID))
            .thenReturn(Optional.empty())

        assertThrows<NotFoundFutureMeException> { service.updateTitle(UpdateFutureMeTitleRequest(title), USER_ID) }
    }

    @Test
    @DisplayName("캐릭터 수정 테스트")
    fun given_type_when_setCharacter_then_success(){
        val futureMe = FutureMe.from(FutureMeCharacterRequest(Character.Type.CHEESE), USER_ID)
        val request = FutureMeCharacterRequest(Character.Type.VEGA)

        Mockito.`when`(repo.findById(USER_ID))
            .thenReturn(Optional.of(futureMe))

        service.updateCharacter(request, USER_ID)
        assertEquals(request.type, futureMe.character.type)
    }

    @Test
    @DisplayName("존재하지 않는 미래의 나 캐릭터 수정 테스트")
    fun given_notExistFutureMe_when_setCharacter_then_throwNotFoundFutureMeException(){
        val request = FutureMeCharacterRequest(Character.Type.VEGA)

        Mockito.`when`(repo.findById(USER_ID))
            .thenReturn(Optional.empty())

        assertThrows<NotFoundFutureMeException> {  service.updateCharacter(request, USER_ID)}
    }

    @Test
    @DisplayName("미래의 나 조회 테스트")
    fun given_user_when_findFutureMe_then_return_futureMe(){
        val futureMe = FutureMe.from(FutureMeCharacterRequest(Character.Type.CHEESE), USER_ID)
        Mockito.`when`(repo.findById(USER_ID))
            .thenReturn(Optional.of(futureMe))

        assertEquals(futureMe, service.findFutureMe(USER_ID))
    }

    @Test
    @DisplayName("존재하지 않는 미래의 나 조회 테스트")
    fun given_userHasNotFutureMe_when_findFutureMe_then_throwNotFoundFutureMeException(){
        Mockito.`when`(repo.findById(USER_ID))
            .thenReturn(Optional.empty())

        assertThrows<NotFoundFutureMeException> {  service.findFutureMe(USER_ID)}
    }

    @Test
    @DisplayName("경험치 상승 테스트")
    fun given_event_when_gainExperiencePoints_then_increaseExperiencePointsForEachEvent(){
        val futureMe = FutureMe.from(FutureMeCharacterRequest(Character.Type.CHEESE), USER_ID)
        var beforeExperiencePoints = futureMe.character.experiencePoints
        val ticketCompletionEvent = Mockito.mock(TicketCompletionEvent::class.java)
        val imaginationCompletionEvent = Mockito.mock(ImaginationCompletionEvent::class.java)

        Mockito.`when`(repo.findById(USER_ID))
            .thenReturn(Optional.of(futureMe))
        Mockito.`when`(ticketCompletionEvent.userID).thenReturn(USER_ID)
        Mockito.`when`(imaginationCompletionEvent.userID).thenReturn(USER_ID)

        service.gainExperiencePoints(ticketCompletionEvent)
        assertEquals(beforeExperiencePoints + 20,futureMe.character.experiencePoints)

        beforeExperiencePoints = futureMe.character.experiencePoints
        service.gainExperiencePoints(imaginationCompletionEvent)
        assertEquals(beforeExperiencePoints + 10,futureMe.character.experiencePoints)
    }

    @Test
    @DisplayName("존재하지 않는 미래의 나 경험치 상승 테스트")
    fun given_notExistFutureMe_when_gainExperiencePoints_then_throwNotFoundFutureMeException(){
        assertThrows<NotFoundFutureMeException> { service.gainExperiencePoints(Mockito.mock(ImaginationCompletionEvent::class.java)) }
    }

    @Test
    @DisplayName("이미 미래의 나가 존재할 때 미래의 나 생성 테스트")
    fun given_requestWithAlreadyExistFutureMe_when_createFutureMe_then_throwIllegalStateException(){
        Mockito.`when`(repo.existsById(USER_ID)).thenReturn(true)
        assertThrows<IllegalStateException> { service.createFutureMe(FutureMeCharacterRequest(Character.Type.CHEESE), USER_ID) }
    }
}
package ac.kr.smu.endticket.futureme.futureme

import ac.kr.smu.endticket.common.web.enum.CharacterType
import ac.kr.smu.endticket.futureme.domain.event.model.Event
import ac.kr.smu.endticket.futureme.domain.event.model.ImaginationCompletedEvent
import ac.kr.smu.endticket.futureme.domain.event.model.TicketCompletedEvent
import ac.kr.smu.endticket.futureme.domain.futureme.exception.FutureMeNotFoundException
import ac.kr.smu.endticket.futureme.domain.futureme.model.Character
import ac.kr.smu.endticket.futureme.domain.futureme.model.FutureMe
import ac.kr.smu.endticket.futureme.domain.futureme.repository.FutureMeRepository
import ac.kr.smu.endticket.futureme.service.FutureMeService
import ac.kr.smu.endticket.futureme.ui.request.CreateFutureMeRequest
import ac.kr.smu.endticket.futureme.ui.request.UpdateFutureMeRequest
import ac.kr.smu.endticket.futureme.ui.response.FutureMeResponse
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
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
    @DisplayName("미래의 나 수정 테스트")
    fun given_request_when_updateFutureMe_then_returnUpdatedFutureMe(){
        val futureMe = FutureMe.from(CreateFutureMeRequest(CharacterType.CHEESE), FutureMeTestParameters.USER_ID)

        Mockito.`when`(repo.findById(FutureMeTestParameters.USER_ID))
            .thenReturn(Optional.of(futureMe))

        val updated = service.updateFutureMe(FutureMeTestParameters.UPDATE_REQUEST, FutureMeTestParameters.USER_ID)

        assertEquals(FutureMeTestParameters.UPDATE_REQUEST.title, updated.title)
        assertEquals(FutureMeTestParameters.UPDATE_REQUEST.characterType, updated.character.type)
    }

    @Test
    @DisplayName("존재하지 않는 미래의 나 수정 테스트")
    fun given_userIdDoesNotHasFutureMe_when_updateFutureMe_then_throwFutureMeNotFoundException(){
        Mockito.`when`(repo.findById(FutureMeTestParameters.USER_ID))
            .thenReturn(Optional.empty())

        assertThrows<FutureMeNotFoundException> { service.updateFutureMe(FutureMeTestParameters.UPDATE_REQUEST, FutureMeTestParameters.USER_ID) }
    }

    @Test
    @DisplayName("미래의 나 조회 테스트")
    fun given_user_when_findFutureMe_then_return_futureMe(){
        val futureMe = FutureMe.from(CreateFutureMeRequest(CharacterType.CHEESE), FutureMeTestParameters.USER_ID)
        Mockito.`when`(repo.findById(FutureMeTestParameters.USER_ID))
            .thenReturn(Optional.of(futureMe))

        assertEquals(futureMe.toResponse(), service.findFutureMe(FutureMeTestParameters.USER_ID))
    }

    @Test
    @DisplayName("존재하지 않는 미래의 나 조회 테스트")
    fun given_userDoesNotHasFutureMe_when_findFutureMe_then_throwFutureMeNotFoundException(){
        Mockito.`when`(repo.findById(FutureMeTestParameters.USER_ID))
            .thenReturn(Optional.empty())

        assertThrows<FutureMeNotFoundException> {  service.findFutureMe(FutureMeTestParameters.USER_ID)}
    }

    @ParameterizedTest
    @DisplayName("경험치 상승 테스트")
    @MethodSource("${FutureMeTestParameters.PATH}#provideFutureMeAndEvent")
    fun given_event_when_gainExperiencePoints_then_increaseExperiencePointsForEachEvent(futureMe: FutureMe, event: Event, amount: Int){

        Mockito.`when`(repo.findById(FutureMeTestParameters.USER_ID))
            .thenReturn(Optional.of(futureMe))

        service.gainExperiencePoints(event)
        assertEquals(amount,futureMe.toResponse().character.experiencePoints)
    }

    @Test
    @DisplayName("존재하지 않는 미래의 나 경험치 상승 테스트")
    fun given_notExistFutureMe_when_gainExperiencePoints_then_throwFutureMeNotFoundException(){
        assertThrows<FutureMeNotFoundException> { service.gainExperiencePoints(Mockito.mock(ImaginationCompletedEvent::class.java)) }
    }

    @Test
    @DisplayName("이미 미래의 나가 존재할 때 미래의 나 생성 테스트")
    fun given_requestWithAlreadyExistFutureMe_when_createFutureMe_then_throwIllegalStateException(){
        Mockito.`when`(repo.existsById(FutureMeTestParameters.USER_ID)).thenReturn(true)
        assertThrows<IllegalStateException> { service.createFutureMe(CreateFutureMeRequest(CharacterType.CHEESE), FutureMeTestParameters.USER_ID) }
    }

    @Test
    @DisplayName("미래의 나 삭제 테스트")
    fun given_userId_when_deleteFutureMe_then_success(){
        service.deleteFutureMe(FutureMeTestParameters.USER_ID)
        Mockito.verify(repo).deleteById(FutureMeTestParameters.USER_ID)
    }
}
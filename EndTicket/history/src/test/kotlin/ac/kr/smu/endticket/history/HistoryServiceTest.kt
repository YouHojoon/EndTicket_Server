package ac.kr.smu.endticket.history

import ac.kr.smu.endticket.common.test.mockAny
import ac.kr.smu.endticket.history.domain.model.History
import ac.kr.smu.endticket.history.domain.repository.HistoryRepository
import ac.kr.smu.endticket.history.infra.messaging.EventResponse
import ac.kr.smu.endticket.history.infra.messaging.TicketCompletedEventResponse
import ac.kr.smu.endticket.history.service.HistoryService
import ac.kr.smu.endticket.history.ui.response.HistoryCount
import ac.kr.smu.endticket.history.ui.response.HistorySlice
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.data.domain.PageRequest
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.core.ValueOperations
import kotlin.test.BeforeTest

@ExtendWith(MockitoExtension::class)
class HistoryServiceTest(
    @Mock
    private val repo: HistoryRepository,
    @Mock
    private val ops: ValueOperations<String, Any>,
    @Mock
    private val redisTemplate: RedisTemplate<String, Any>,
) {
    @InjectMocks
    private lateinit var service: HistoryService

    private companion object {
        const val HISTORY_COUNT_REDIS_KEY = "history-count::${HistoryTestParameters.USER_ID}"
    }

    @BeforeTest
    fun init() {
        Mockito.`when`(redisTemplate.opsForValue()).thenReturn(ops)
        MockitoAnnotations.openMocks(this)
    }

    @ParameterizedTest
    @DisplayName("기록 조회 테스트")
    @MethodSource("${HistoryTestParameters.PATH}#provideHistoriesAndType")
    fun given_specificIdAndType_when_findHistories_then_returnHistoryResponses(
        histories: Set<History>,
        type: History.Type,
    ) {
        val pageable = PageRequest.of(0, 10)

        Mockito
            .`when`(repo.findAllByUserIdAndType(HistoryTestParameters.USER_ID, type, PageRequest.of(0, 10)))
            .thenReturn(HistorySlice(histories, pageable))

        val entities = service.findHistories(HistoryTestParameters.USER_ID, type, pageable)

        assert(entities.isNotEmpty())
        Mockito.verify(repo).findAllByUserIdAndType(HistoryTestParameters.USER_ID, type, pageable)
    }

    @Test
    @DisplayName("기록 개수 조회 테스트")
    fun given_userId_when_findHistoryCount_then_returnHistoryCount() {
        Mockito
            .`when`(repo.countEachHistoryByUserId(HistoryTestParameters.USER_ID))
            .thenReturn(HistoryCount(1, 5, 1))

        service.findHistoryCount(HistoryTestParameters.USER_ID)

        Mockito.verify(repo).countEachHistoryByUserId(HistoryTestParameters.USER_ID)
    }

    @ParameterizedTest
    @DisplayName("기록 저장 테스트")
    @MethodSource("${HistoryTestParameters.PATH}#provideEventResponseAndUserId")
    fun given_eventResponseAndUserId_when_saveHistory_then_success(
        eventResponse: EventResponse,
        userId: Long,
    ) {
        val type = if (eventResponse is TicketCompletedEventResponse) History.Type.TICKET else History.Type.IMAGINATION

        Mockito
            .`when`(repo.existsBySpecificIdAndType(eventResponse.id, type))
            .thenReturn(false)
        Mockito
            .`when`(ops.get(HISTORY_COUNT_REDIS_KEY))
            .thenReturn(null)

        service.saveHistory(eventResponse, userId)

        Mockito.verify(repo).existsBySpecificIdAndType(eventResponse.id, type)
        Mockito.verify(repo).save(mockAny())
        Mockito.verify(ops).get(HISTORY_COUNT_REDIS_KEY)
    }

    @ParameterizedTest
    @DisplayName("기록 저장 시 기록 개수 증가 테스트")
    @MethodSource("${HistoryTestParameters.PATH}#provideEventResponseAndUserId")
    fun given_eventResponseAndUserIdWithHistoryCountInRedis_when_saveHistory_then_saveHistoryAndUpdateHistoryCount(
        eventResponse: EventResponse,
        userId: Long,
    ) {
        val type = if (eventResponse is TicketCompletedEventResponse) History.Type.TICKET else History.Type.IMAGINATION
        Mockito
            .`when`(repo.existsBySpecificIdAndType(eventResponse.id, type))
            .thenReturn(false)
        Mockito
            .`when`(ops.get(HISTORY_COUNT_REDIS_KEY))
            .thenReturn(HistoryCount(0, 0, 0))

        service.saveHistory(eventResponse, userId)

        Mockito.verify(repo).save(mockAny())
        Mockito.verify(ops).set(
            HISTORY_COUNT_REDIS_KEY,
            when (type) {
                History.Type.TICKET -> HistoryCount(1, (eventResponse as TicketCompletedEventResponse).swipeCount, 0)
                History.Type.IMAGINATION -> HistoryCount(0, 0, 1)
            },
        )
        Mockito.verify(repo).existsBySpecificIdAndType(eventResponse.id, type)
    }

    @ParameterizedTest
    @DisplayName("기록 중복 저장 테스트")
    @MethodSource("${HistoryTestParameters.PATH}#provideEventResponseAndUserId")
    fun given_eventResponseAlreadySaved_when_saveHistory_then_doNothing(
        eventResponse: EventResponse,
        userId: Long,
    ) {
        val type = if (eventResponse is TicketCompletedEventResponse) History.Type.TICKET else History.Type.IMAGINATION

        Mockito
            .`when`(repo.existsBySpecificIdAndType(eventResponse.id, type))
            .thenReturn(true)

        service.saveHistory(eventResponse, userId)

        Mockito.verify(repo, Mockito.only()).existsBySpecificIdAndType(eventResponse.id, type)
    }
}

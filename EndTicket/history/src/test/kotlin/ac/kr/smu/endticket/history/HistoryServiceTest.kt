package ac.kr.smu.endticket.history

import ac.kr.smu.endticket.history.domain.model.History
import ac.kr.smu.endticket.history.domain.model.ImaginationHistory
import ac.kr.smu.endticket.history.domain.model.TicketHistory
import ac.kr.smu.endticket.history.ui.response.HistorySlice
import ac.kr.smu.endticket.history.domain.repository.HistoryRepository
import ac.kr.smu.endticket.history.service.HistoryService
import ac.kr.smu.endticket.history.ui.response.HistoryCount
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
import kotlin.test.BeforeTest

@ExtendWith(MockitoExtension::class)
class HistoryServiceTest(
    @Mock
    private val repo: HistoryRepository
) {
    @InjectMocks
    private lateinit var service: HistoryService

    @BeforeTest
    fun init(){
        MockitoAnnotations.openMocks(this)
    }

    @ParameterizedTest
    @DisplayName("기록 조회 테스트")
    @MethodSource("${HistoryTestParameters.PATH}#provideHistoriesAndType")
    fun given_specificIdAndType_when_findHistories_then_returnHistoryResponses(histories: Set<History>, type: History.Type){
        val pageable =  PageRequest.of(0,10)

        Mockito.`when`(repo.findAllByUserIdAndType(HistoryTestParameters.USER_ID, type, PageRequest.of(0,10)))
            .thenReturn(HistorySlice(histories, pageable))

        val entities = service.findHistories(HistoryTestParameters.USER_ID, type,pageable)

        assert(entities.isNotEmpty())
        Mockito.verify(repo).findAllByUserIdAndType(HistoryTestParameters.USER_ID, type, pageable)
    }

    @Test
    @DisplayName("기록 개수 조회 테스트")
    fun given_userId_when_findHistoryCount_then_returnHistoryCount(){
        Mockito.`when`(repo.countEachHistoryByUserId(HistoryTestParameters.USER_ID))
            .thenReturn(HistoryCount(1,5,1))

        service.findHistoryCount(HistoryTestParameters.USER_ID)

        Mockito.verify(repo).countEachHistoryByUserId(HistoryTestParameters.USER_ID)
    }
}
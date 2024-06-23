package ac.kr.smu.endticket.history

import ac.kr.smu.endticket.history.domain.model.History
import ac.kr.smu.endticket.history.domain.model.ImaginationHistory
import ac.kr.smu.endticket.history.domain.model.TicketHistory
import ac.kr.smu.endticket.history.domain.repository.HistoryRepository
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.data.domain.PageRequest
import kotlin.reflect.KClass
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class HistoryRepositoryTest @Autowired constructor(
    private val repo: HistoryRepository
) {
    @ParameterizedTest
    @DisplayName("기록 조회 테스트")
    @MethodSource("${HistoryTestParameters.PATH}#provideHistorySpecificIdAndType")
    fun given_specificIdAndType_when_findBySpecificIdAndType_then_returnHistory(history: History, specificId: Long, type: History.Type){
        repo.save(history)

        val entity = repo.findBySpecificIdAndType(specificId, type)

        assertNotNull(entity)
        assertEquals(history, entity)
    }

    @ParameterizedTest
    @DisplayName("존재 여부 테스트")
    @MethodSource("${HistoryTestParameters.PATH}#provideHistorySpecificIdAndType")
    fun given_specificIdAndType_existsSpecificIdAndType_then_returnIsExists(history: History, specificId: Long, type: History.Type){
        repo.save(history)

        assertTrue(repo.existsBySpecificIdAndType(specificId, type))
    }

    @ParameterizedTest
    @DisplayName("사용자의 기록들 조회 테스트")
    @MethodSource("${HistoryTestParameters.PATH}#provideHistoriesAndType")
    fun given_userIdAndType_findAllByUserIdAndType_then_returnHistories(histories: Collection<out History>, type: History.Type) {
        repo.saveAll(histories)
        val entities = repo.findAllByUserIdAndType(HistoryTestParameters.USER_ID, type, PageRequest.of(0, 10))

        for ((history, entity) in histories.zip(entities)) {
            assertEquals(history, entity)
        }
    }

    @ParameterizedTest
    @DisplayName("사용자의 기록 개수들 조회 테스트")
    @MethodSource("${HistoryTestParameters.PATH}#provideHistoriesOfEachType")
    fun given_userId_when_countEachHistoryByUserId_then_returnHistoryCount(ticketHistories:Set<TicketHistory>, imaginationHistories: Set<ImaginationHistory>){
        repo.saveAll(ticketHistories)
        repo.saveAll(imaginationHistories)

        val count = repo.countEachHistoryByUserId(HistoryTestParameters.USER_ID)

        assertEquals(ticketHistories.size, count.ticketHistoryCount)
        assertEquals(imaginationHistories.size, count.imaginationHistoryCount)
    }
}
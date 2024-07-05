package ac.kr.smu.endticket.futureme.event

import ac.kr.smu.endticket.futureme.domain.event.model.ImaginationCompletedEvent
import ac.kr.smu.endticket.futureme.domain.event.model.TicketCompletedEvent
import ac.kr.smu.endticket.futureme.domain.event.repository.EventRepository
import ac.kr.smu.endticket.futureme.domain.imagination.model.Imagination
import ac.kr.smu.endticket.futureme.domain.imagination.repository.ImaginationRepository
import ac.kr.smu.endticket.futureme.imagination.ImaginationParameters
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class EventRepositoryTest
    @Autowired
    constructor(
        private val repo: EventRepository,
        private val imaginationRepository: ImaginationRepository,
    ) {
        @Test
        @DisplayName("미전송 상상해보기 완료 이벤트 조회 테스트")
        fun given_date_when_findNotSentEventBefore_then_returnEvents() {
            val imagination =
                imaginationRepository.save(Imagination.from(ImaginationParameters.REQUEST, ImaginationParameters.USER_ID))
            val event = ImaginationCompletedEvent(imagination)
            val now = event.audit.createdAt.plusMinutes(10)

            repo.save(event)
            val entity = repo.findNotSentEventBefore(now).firstOrNull()

            assertNotNull(entity)
            assertEquals(event, entity)
        }

        @Test
        @DisplayName("이벤트 존재 여부 조회 테스트")
        fun given_specificIdAndType_when_existsBySpecificIdAndType_then_returnExistence() {
            val specificId = 1L
            val event = TicketCompletedEvent(specificId, ImaginationParameters.USER_ID)

            repo.save(event)
            assertTrue(repo.existsBySpecificIdAndType(specificId, TicketCompletedEvent::class))
        }
    }

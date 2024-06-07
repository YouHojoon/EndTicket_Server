package ac.kr.smu.endTicket.futureMe

import ac.kr.smu.endTicket.common.kafka.constant.KafkaTopic
import ac.kr.smu.endTicket.common.kafka.test.createKafkaContainer
import ac.kr.smu.endTicket.common.kafka.test.messageListener
import ac.kr.smu.endTicket.futureMe.domain.event.model.Event
import ac.kr.smu.endTicket.futureMe.domain.event.model.ImaginationCompletionEvent
import ac.kr.smu.endTicket.futureMe.domain.event.repository.EventRepository
import ac.kr.smu.endTicket.futureMe.domain.imagination.model.Imagination
import ac.kr.smu.endTicket.futureMe.job.ImaginationCompletionEventJob
import ac.kr.smu.endTicket.futureMe.service.ImaginationCompletionEventMessageService
import ac.kr.smu.endTicket.futureMe.ui.response.ImaginationCompletionEventResponse
import ac.kr.smu.endTicket.futureMe.ui.response.TicketResponse
import ac.kr.smu.endTicket.test.mockAny
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
import org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.kafka.listener.KafkaMessageListenerContainer
import org.springframework.kafka.test.EmbeddedKafkaBroker
import org.springframework.kafka.test.context.EmbeddedKafka
import org.springframework.scheduling.annotation.SchedulingConfiguration
import java.util.concurrent.LinkedBlockingQueue
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@SpringBootTest(
    classes = [
        KafkaAutoConfiguration::class,
        SchedulingConfiguration::class,
        ImaginationCompletionEventJob::class,
        ImaginationCompletionEventMessageService::class,
    ]
)
@EmbeddedKafka(
    partitions = 3,
    brokerProperties = [
        "listeners=PLAINTEXT://localhost:9292"
    ],
    ports = [9292]
)
class ImaginationCompletionEvenJobTest @Autowired constructor(
    @MockBean
    private val repo: EventRepository,
    private val broker: EmbeddedKafkaBroker
) {
    private lateinit var container: KafkaMessageListenerContainer<String, ImaginationCompletionEventResponse>

    @Test
    @DisplayName("전송되지 않은 상상해보기 완료 이벤트 재전송 테스트")
    fun given_notSentImaginationCompletionEvent_when_resendImaginationCompletionEvent_then_resendMessage_and_save(){
        val events = setOf(ImaginationCompletionEvent(
            Imagination.from(request, USER_ID)
        ))
        val queue = LinkedBlockingQueue<ConsumerRecord<String, ImaginationCompletionEventResponse>>()
        container = createKafkaContainer(broker, KafkaTopic.IMAGINATION_COMPLETION)
        container.messageListener(broker){
            queue.add(it)
        }

        Mockito.`when`(repo.findNotSentEventBefore(mockAny()))
            .thenReturn(events)

        Thread.sleep(500)

        assertTrue(queue.isNotEmpty())

        for ((event, record) in events.zip(queue)){
            val message = event.toMessage()

            assertEquals(message.key, record.key().toLong())
            assertEquals(message.payload.behavior, record.value().behavior)
            assertEquals(message.payload.target, record.value().target)
            assertEquals(message.payload.color, record.value().color)

        }

        Mockito.verify(repo, Mockito.atLeast(1)).saveAll(mockAny<Collection<ImaginationCompletionEvent>>())
    }
}
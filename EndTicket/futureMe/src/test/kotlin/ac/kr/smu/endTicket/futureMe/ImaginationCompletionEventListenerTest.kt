package ac.kr.smu.endTicket.futureMe

import ac.kr.smu.endTicket.common.kafka.constant.KafkaTopic
import ac.kr.smu.endTicket.common.kafka.test.createKafkaContainer
import ac.kr.smu.endTicket.common.kafka.test.messageListener
import ac.kr.smu.endTicket.futureMe.domain.event.repository.EventRepository
import ac.kr.smu.endTicket.futureMe.domain.event.model.ImaginationCompletionEvent
import ac.kr.smu.endTicket.futureMe.domain.imagination.model.Imagination
import ac.kr.smu.endTicket.futureMe.listener.ImaginationCompletionEventListener
import ac.kr.smu.endTicket.futureMe.service.FutureMeEventService
import ac.kr.smu.endTicket.futureMe.service.ImaginationCompletionEventMessageService
import ac.kr.smu.endTicket.futureMe.ui.response.ImaginationCompletionEventResponse
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.kafka.listener.KafkaMessageListenerContainer
import org.springframework.kafka.test.EmbeddedKafkaBroker
import org.springframework.kafka.test.context.EmbeddedKafka
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.TimeUnit
import kotlin.test.assertEquals

@SpringBootTest(
    classes = [
        ImaginationCompletionEventMessageService::class,
        ImaginationCompletionEventListener::class,
        FutureMeEventService::class,
        KafkaAutoConfiguration::class
    ]
)
@EmbeddedKafka(
    partitions = 3,
    ports = [9292],
    brokerProperties = [
        "listeners=PLAINTEXT://localhost:9292"
    ]
)
class ImaginationCompletionEventListenerTest @Autowired constructor(
    @MockBean
    private val repo: EventRepository,

    private val broker: EmbeddedKafkaBroker,
    private val eventService: FutureMeEventService
) {
    private lateinit var container: KafkaMessageListenerContainer<String, ImaginationCompletionEventResponse>

    @Test
    @DisplayName("상상해보기 완료 이벤트 테스트")
    fun given_imaginationCompletionEvent_then_saveEvent_and_sendMessage(){
        val imagination = Imagination.from(request, USER_ID)
        val event = ImaginationCompletionEvent(imagination)

        container = createKafkaContainer(broker, KafkaTopic.IMAGINATION_COMPLETION)

        val queue = LinkedBlockingQueue<ConsumerRecord<String, ImaginationCompletionEventResponse>>()
        container.messageListener(broker){
            queue.add(it)
        }

        eventService.eventPublish(event)
        val record = queue.poll(1000, TimeUnit.MILLISECONDS)
        val expectPayload = event.toMessage().payload

        Mockito.verify(repo, Mockito.times(2)).save(event)
        assertEquals(expectPayload.behavior, record.value().behavior)
        assertEquals(expectPayload.target, record.value().target)
        assertEquals(expectPayload.color, record.value().color)
        assertEquals(event.userID.toString(), record.key())

        container.stop()
    }
}
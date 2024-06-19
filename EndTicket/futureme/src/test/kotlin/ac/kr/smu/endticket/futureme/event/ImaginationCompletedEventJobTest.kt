package ac.kr.smu.endticket.futureme.event

import KafkaMessageService
import ac.kr.smu.endTicket.test.mockAny
import ac.kr.smu.endticket.common.kafka.constant.KafkaTopic
import ac.kr.smu.endticket.common.kafka.test.createKafkaContainer
import ac.kr.smu.endticket.common.kafka.test.messageListener
import ac.kr.smu.endticket.futureme.domain.event.model.Event
import ac.kr.smu.endticket.futureme.domain.event.model.ImaginationCompletedEvent
import ac.kr.smu.endticket.futureme.domain.event.repository.EventRepository
import ac.kr.smu.endticket.futureme.domain.imagination.model.Imagination
import ac.kr.smu.endticket.futureme.imagination.USER_ID
import ac.kr.smu.endticket.futureme.imagination.request
import ac.kr.smu.endticket.futureme.infra.messaging.ImaginationCompletedEventResponse
import ac.kr.smu.endticket.futureme.job.ImaginationCompletedEventJob
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.boot.test.mock.mockito.SpyBean
import org.springframework.kafka.listener.KafkaMessageListenerContainer
import org.springframework.kafka.test.EmbeddedKafkaBroker
import org.springframework.kafka.test.context.EmbeddedKafka
import java.util.concurrent.CompletableFuture
import java.util.concurrent.LinkedBlockingQueue
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@SpringBootTest(
    classes = [
        KafkaAutoConfiguration::class,
        ImaginationCompletedEventJob::class,
    ]
)
@EmbeddedKafka(partitions = 3)
class ImaginationCompletedEventJobTest @Autowired constructor(
    @MockBean
    private val repo: EventRepository,
    private val broker: EmbeddedKafkaBroker,
    private val job: ImaginationCompletedEventJob
) {
    @SpyBean
    private lateinit var messageService: KafkaMessageService<String, ImaginationCompletedEventResponse>
    private lateinit var container: KafkaMessageListenerContainer<String, ImaginationCompletedEventResponse>
    private val queue = LinkedBlockingQueue<ConsumerRecord<String, ImaginationCompletedEventResponse>>()

    @BeforeEach
    fun init(){
        container = createKafkaContainer(broker, KafkaTopic.IMAGINATION_COMPLETION)
        container.messageListener(broker){
            queue.add(it)
        }
    }
    @AfterEach
    fun reset(){
        container.stop()
    }
    @Test
    @DisplayName("전송되지 않은 상상해보기 완료 이벤트 재전송 테스트")
    fun given_notSentImaginationCompletionEvent_when_resendImaginationCompletionEvent_then_resendMessage_and_save(){
        val events = setOf(
            ImaginationCompletedEvent(
            Imagination.from(request, USER_ID)
        )
        )

        Mockito.`when`(repo.findNotSentEventBefore(mockAny()))
            .thenReturn(events)

        job.resendImaginationCompletionEvent()
        Thread.sleep(1000)

        assertTrue(queue.isNotEmpty())
        for ((event, record) in events.zip(queue)){
            val message = event.toMessage()

            assertEquals(message.key, record.key())
            assertEquals(message.payload.behavior, record.value().behavior)
            assertEquals(message.payload.target, record.value().target)
            assertEquals(message.payload.color, record.value().color)
        }

        Mockito.verify(repo, Mockito.atLeast(1)).saveAll(mockAny<Collection<ImaginationCompletedEvent>>())
    }

    @Test
    @DisplayName("이벤트 재전송 실패 테스트")
    fun given_notSentImaginationCompletionEvent_when_resendImaginationCompletionEventFail_then_doNothing() {
        val event = ImaginationCompletedEvent(Imagination.from(request, USER_ID))
        val events = setOf(event)

        Mockito.doReturn(events)
            .`when`(repo).findNotSentEventBefore(mockAny())
        Mockito.`when`(messageService.send(Mockito.anyString(), Mockito.anyCollection()))
            .thenReturn(listOf(CompletableFuture.failedFuture(RuntimeException())))

        job.resendImaginationCompletionEvent()
        Thread.sleep(1000L)

        Mockito.verify(repo, Mockito.times(1)).saveAll(Mockito.argThat<List<Event>> { it.isEmpty() })
    }
}
package ac.kr.smu.endTicket.futureMe.imagination

import KafkaMessageService
import ac.kr.smu.endTicket.common.kafka.constant.KafkaTopic
import ac.kr.smu.endTicket.common.kafka.test.createKafkaContainer
import ac.kr.smu.endTicket.common.kafka.test.messageListener
import ac.kr.smu.endTicket.futureMe.domain.event.model.ImaginationCompletionEvent
import ac.kr.smu.endTicket.futureMe.domain.event.repository.EventRepository
import ac.kr.smu.endTicket.futureMe.domain.imagination.model.Imagination
import ac.kr.smu.endTicket.futureMe.listener.ImaginationCompletionEventListener
import ac.kr.smu.endTicket.futureMe.service.FutureMeEventService
import ac.kr.smu.endTicket.futureMe.infra.messaging.ImaginationCompletionEventResponse
import ac.kr.smu.endTicket.futureMe.service.FutureMeService
import ac.kr.smu.endTicket.test.mockAny
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
import java.util.concurrent.TimeUnit
import kotlin.test.assertEquals

@SpringBootTest(
    classes = [
        ImaginationCompletionEventListener::class,
        FutureMeEventService::class,
        KafkaAutoConfiguration::class,
    ]
)
@EmbeddedKafka(partitions = 3)
class ImaginationCompletionEventListenerTest @Autowired constructor(
    @MockBean
    private val repo: EventRepository,
    @MockBean
    private val futureMeService: FutureMeService,
    private val broker: EmbeddedKafkaBroker,
    private val eventService: FutureMeEventService
) {
    @SpyBean
    private lateinit var messageService: KafkaMessageService<String, ImaginationCompletionEventResponse>
    private lateinit var container: KafkaMessageListenerContainer<String, ImaginationCompletionEventResponse>
    private val queue = LinkedBlockingQueue<ConsumerRecord<String, ImaginationCompletionEventResponse>>()

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
    @DisplayName("상상해보기 완료 이벤트 테스트")
    fun given_imaginationCompletionEvent_then_saveEvent_and_sendMessage(){
        val imagination = Imagination.from(request, USER_ID)
        val event = ImaginationCompletionEvent(imagination)

        eventService.eventPublish(event)

        val record = queue.poll(1000, TimeUnit.MILLISECONDS)
        val expectPayload = event.toMessage().payload

        Mockito.verify(futureMeService, Mockito.times(1)).gainExperiencePoints(event)
        Mockito.verify(repo, Mockito.times(2)).save(event)

        assertEquals(expectPayload.behavior, record.value().behavior)
        assertEquals(expectPayload.target, record.value().target)
        assertEquals(expectPayload.color, record.value().color)
        assertEquals(event.userID.toString(), record.key())

        container.stop()
    }
    @Test
    @DisplayName("상상해보기 완료 이벤트 메시지 전송 실패 테스트")
    fun given_imaginationCompletionEvent_when_sendMessageFail_then_doNothing(){
        val event = ImaginationCompletionEvent(Imagination.from(request, USER_ID))
        val mockEvent = Mockito.spy(event)
        val message = event.toMessage()

        Mockito.`when`(mockEvent.toMessage())
            .thenReturn(message)
        Mockito.`when`(messageService.send(KafkaTopic.IMAGINATION_COMPLETION, message))
            .thenReturn(CompletableFuture.failedFuture(RuntimeException()))

        eventService.eventPublish(mockEvent)

        Mockito.verify(repo, Mockito.only()).save(mockEvent)
        Mockito.verify(futureMeService, Mockito.times(1)).gainExperiencePoints(mockEvent)
    }
}
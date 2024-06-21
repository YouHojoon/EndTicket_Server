package ac.kr.smu.endticket.futureme.event

import KafkaMessageService
import ac.kr.smu.endticket.common.kafka.constant.KafkaTopic
import ac.kr.smu.endticket.common.kafka.test.createKafkaContainer
import ac.kr.smu.endticket.common.kafka.test.messageListener
import ac.kr.smu.endticket.common.test.mockAny
import ac.kr.smu.endticket.common.web.enum.CharacterType
import ac.kr.smu.endticket.futureme.domain.event.model.ImaginationCompletedEvent
import ac.kr.smu.endticket.futureme.domain.event.repository.EventRepository
import ac.kr.smu.endticket.futureme.domain.futureme.model.FutureMe
import ac.kr.smu.endticket.futureme.domain.imagination.model.Imagination
import ac.kr.smu.endticket.futureme.imagination.REQUEST
import ac.kr.smu.endticket.futureme.imagination.USER_ID
import ac.kr.smu.endticket.futureme.infra.messaging.ImaginationCompletedEventResponse
import ac.kr.smu.endticket.futureme.listener.ImaginationCompletedEventListener
import ac.kr.smu.endticket.futureme.service.FutureMeEventService
import ac.kr.smu.endticket.futureme.service.FutureMeService
import ac.kr.smu.endticket.futureme.ui.request.CreateFutureMeRequest
import ac.kr.smu.endticket.futureme.ui.response.FutureMeResponse
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
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

@SpringBootTest(
    classes = [
        ImaginationCompletedEventListener::class,
        FutureMeEventService::class,
        KafkaAutoConfiguration::class,
    ]
)
@EmbeddedKafka(partitions = 3)
class ImaginationCompletedEventListenerTest @Autowired constructor(
    @MockBean
    private val repo: EventRepository,
    @MockBean
    private val futureMeService: FutureMeService,
    @SpyBean
    private val messageService: KafkaMessageService<String, ImaginationCompletedEventResponse>,
    private val broker: EmbeddedKafkaBroker,
    private val eventService: FutureMeEventService
) {
    private lateinit var container: KafkaMessageListenerContainer<String, ImaginationCompletedEventResponse>
    private val queue = LinkedBlockingQueue<ConsumerRecord<String, ImaginationCompletedEventResponse>>()

    @BeforeEach
    fun init(){
        container = createKafkaContainer(broker, KafkaTopic.IMAGINATION_COMPLETION)
        container.messageListener(broker){
            queue.add(it)
        }
        Mockito.`when`(futureMeService.findFutureMe(USER_ID))
            .thenReturn(FutureMeResponse.from(FUTURE_ME))
    }
    @AfterEach
    fun reset(){
        container.stop()
    }

    @DisplayName("상상해보기 완료 이벤트 테스트")
    @ParameterizedTest
    @MethodSource("${EventTestParameters.PATH}#provideImaginationCompletedEvent")
    fun given_imaginationCompletedEvent_then_saveEvent_and_sendMessage(event: ImaginationCompletedEvent){
        eventService.publishEvent(event)
        Thread.sleep(1000L)

        Mockito.verify(futureMeService, Mockito.times(1)).gainExperiencePoints(event)
        Mockito.verify(repo, Mockito.times(2)).save(mockAny())

        val record = queue.poll()
        val expectPayload = event.toMessage(FUTURE_ME.character.type).payload

        assertEquals(expectPayload.behavior, record.value().behavior)
        assertEquals(expectPayload.target, record.value().target)
        assertEquals(expectPayload.color, record.value().color)
        assertEquals(expectPayload.characterType, record.value().characterType)
        assertEquals(event.userId.toString(), record.key())
    }
    @ParameterizedTest
    @DisplayName("상상해보기 완료 이벤트 메시지 전송 실패 테스트")
    @MethodSource("${EventTestParameters.PATH}#provideImaginationCompletedEvent")
    fun given_imaginationCompletedEvent_when_sendMessageFail_then_doNothing(event: ImaginationCompletedEvent){
        val mockEvent = Mockito.spy(event)
        val message = event.toMessage(FUTURE_ME.character.type)

        Mockito.`when`(mockEvent.toMessage(FUTURE_ME.character.type))
            .thenReturn(message)
        Mockito.`when`(messageService.send(KafkaTopic.IMAGINATION_COMPLETION, message))
            .thenReturn(CompletableFuture.failedFuture(RuntimeException()))

        eventService.publishEvent(mockEvent)

        Mockito.verify(repo, Mockito.only()).save(mockEvent)
        Mockito.verify(futureMeService, Mockito.times(1)).gainExperiencePoints(mockEvent)
    }
}
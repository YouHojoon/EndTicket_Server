package ac.kr.smu.endticket.futureme.event

import KafkaMessageService
import ac.kr.smu.endticket.common.kafka.constant.KafkaTopic
import ac.kr.smu.endticket.common.kafka.test.createKafkaContainer
import ac.kr.smu.endticket.common.kafka.test.messageListener
import ac.kr.smu.endticket.common.test.mockAny
import ac.kr.smu.endticket.futureme.domain.event.model.ImaginationCompletedEvent
import ac.kr.smu.endticket.futureme.domain.event.repository.EventRepository
import ac.kr.smu.endticket.futureme.imagination.ImaginationParameters
import ac.kr.smu.endticket.futureme.infra.messaging.ImaginationCompletedEventResponse
import ac.kr.smu.endticket.futureme.listener.ImaginationCompletedEventListener
import ac.kr.smu.endticket.futureme.service.FutureMeEventService
import ac.kr.smu.endticket.futureme.service.FutureMeService
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
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
    ],
)
@EmbeddedKafka(partitions = 3)
class ImaginationCompletedEventListenerTest
    @Autowired
    constructor(
        @MockBean
        private val repo: EventRepository,
        @MockBean
        private val futureMeService: FutureMeService,
        @SpyBean
        private val messageService: KafkaMessageService<String, ImaginationCompletedEventResponse>,
        private val broker: EmbeddedKafkaBroker,
        private val eventService: FutureMeEventService,
    ) {
        private lateinit var container: KafkaMessageListenerContainer<String, ImaginationCompletedEventResponse>
        private val queue = LinkedBlockingQueue<ConsumerRecord<String, ImaginationCompletedEventResponse>>()

        @BeforeEach
        fun init() {
            container = createKafkaContainer(broker, KafkaTopic.IMAGINATION_COMPLETED)
            container.messageListener(broker) {
                queue.add(it)
            }
            Mockito
                .`when`(futureMeService.findFutureMe(ImaginationParameters.USER_ID))
                .thenReturn(EventTestParameters.FUTURE_ME.toResponse())
        }

        @AfterEach
        fun reset() {
            container.stop()
        }

        @DisplayName("상상해보기 완료 이벤트 테스트")
        @ParameterizedTest
        @MethodSource("${EventTestParameters.PATH}#provideImaginationCompletedEvent")
        fun given_imaginationCompletedEvent_then_saveEventAndSendMessage(event: ImaginationCompletedEvent) {
            eventService.publish(event)
            Thread.sleep(1000L)

            Mockito.verify(futureMeService, Mockito.times(1)).gainExperiencePoints(event)
            Mockito.verify(repo).delete(mockAny())

            val record = queue.poll()
            val expectPayload = event.toMessage(EventTestParameters.FUTURE_ME.characterType).payload

            assertEquals(expectPayload?.behavior, record.value().behavior)
            assertEquals(expectPayload?.target, record.value().target)
            assertEquals(expectPayload?.color, record.value().color)
            assertEquals(expectPayload?.characterType, record.value().characterType)
            assertEquals(event.userId.toString(), record.key())
        }

        @ParameterizedTest
        @DisplayName("상상해보기 완료 이벤트 메시지 전송 실패 테스트")
        @MethodSource("${EventTestParameters.PATH}#provideImaginationCompletedEvent")
        fun given_imaginationCompletedEvent_when_sendMessageFail_then_doNothing(event: ImaginationCompletedEvent) {
            val mockEvent = Mockito.spy(event)
            val message = event.toMessage(EventTestParameters.FUTURE_ME.characterType)

            Mockito
                .`when`(mockEvent.toMessage(EventTestParameters.FUTURE_ME.characterType))
                .thenReturn(message)
            Mockito
                .`when`(messageService.send(KafkaTopic.IMAGINATION_COMPLETED, message))
                .thenReturn(CompletableFuture.failedFuture(RuntimeException()))

            eventService.publish(mockEvent)

            Mockito.verify(repo, Mockito.only()).save(mockEvent)
            Mockito.verify(futureMeService, Mockito.times(1)).gainExperiencePoints(mockEvent)
        }
    }

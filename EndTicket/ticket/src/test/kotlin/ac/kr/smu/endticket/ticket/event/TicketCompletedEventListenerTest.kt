package ac.kr.smu.endticket.ticket.event

import KafkaMessageService
import ac.kr.smu.endticket.common.kafka.constant.KafkaTopic
import ac.kr.smu.endticket.common.kafka.test.createKafkaContainer
import ac.kr.smu.endticket.common.kafka.test.messageListener
import ac.kr.smu.endticket.common.test.mockAny
import ac.kr.smu.endticket.ticket.domain.model.TicketCompletedEvent
import ac.kr.smu.endticket.ticket.domain.repository.TicketCompletedEventRepository
import ac.kr.smu.endticket.ticket.infra.messaging.TicketCompletedEventResponse
import ac.kr.smu.endticket.ticket.listener.TicketCompletedEventListener
import ac.kr.smu.endticket.ticket.service.TicketCompletedEventService
import org.apache.kafka.clients.consumer.ConsumerRecord
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
import java.util.concurrent.TimeUnit
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@SpringBootTest(
    classes = [
        TicketCompletedEventService::class,
        TicketCompletedEventListener::class,
        KafkaAutoConfiguration::class,
    ],
)
@EmbeddedKafka
class TicketCompletedEventListenerTest
    @Autowired
    constructor(
        @MockBean
        private val repo: TicketCompletedEventRepository,
        @SpyBean
        private val messageService: KafkaMessageService<String, TicketCompletedEventResponse>,
        private val eventService: TicketCompletedEventService,
        private val broker: EmbeddedKafkaBroker,
    ) {
        private lateinit var container: KafkaMessageListenerContainer<String, TicketCompletedEventResponse>

        @BeforeTest
        fun init() {
            container = createKafkaContainer(broker, KafkaTopic.TICKET_COMPLETED)
        }

        @AfterTest
        fun reset() {
            container.stop()
        }

        @ParameterizedTest
        @DisplayName("티켓 완료 이벤트 테스트")
        @MethodSource("${EventTestParameters.PATH}#provideEvent")
        fun given_ticketCompletedEvent_then_saveEventAndSendMessage(event: TicketCompletedEvent) {
            val queue = LinkedBlockingQueue<ConsumerRecord<String, TicketCompletedEventResponse>>()

            container.messageListener(broker) {
                queue.add(it)
            }

            eventService.publishEvent(event)

            val record = queue.poll(500, TimeUnit.MILLISECONDS)
            val message = event.toMessage()

            Mockito.verify(repo, Mockito.times(1)).save(mockAny())
            Mockito.verify(repo).delete(mockAny())
            assertNotNull(record)
            assertEquals(message.payload?.id, record.value().id)
            assertEquals(message.payload?.behavior, record.value().behavior)
            assertEquals(message.payload?.target, record.value().target)
            assertEquals(message.payload?.color, record.value().color)
            assertEquals(message.payload?.swipeCount, record.value().swipeCount)
            assertEquals(message.key, record.key())
        }

        @ParameterizedTest
        @DisplayName("티켓 완료 이벤트 전송 실패 테스트")
        @MethodSource("${EventTestParameters.PATH}#provideEvent")
        fun given_ticketCompletedEvent_when_sendMessageFail_then_doNothing(event: TicketCompletedEvent) {
            val message = event.toMessage()
            val mockEvent = Mockito.mock(TicketCompletedEvent::class.java)
            Mockito
                .`when`(mockEvent.toMessage())
                .thenReturn(message)
            Mockito
                .`when`(messageService.send(KafkaTopic.TICKET_COMPLETED, message))
                .thenReturn(CompletableFuture.failedFuture(RuntimeException()))

            eventService.publishEvent(mockEvent)

            Mockito.verify(repo, Mockito.only()).save(mockEvent)
        }
    }

package ac.kr.smu.endticket.ticket.event

import KafkaMessageService
import ac.kr.smu.endticket.common.kafka.constant.KafkaTopic
import ac.kr.smu.endticket.common.kafka.test.createKafkaContainer
import ac.kr.smu.endticket.common.kafka.test.messageListener
import ac.kr.smu.endticket.common.test.mockAny
import ac.kr.smu.endticket.ticket.domain.model.TicketCompletedEvent
import ac.kr.smu.endticket.ticket.domain.repository.TicketCompletedEventRepository
import ac.kr.smu.endticket.ticket.infra.messaging.TicketCompletedEventResponse
import ac.kr.smu.endticket.ticket.job.TicketCompletedEventJob
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.junit.jupiter.api.AfterEach
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
import java.util.concurrent.LinkedBlockingQueue
import kotlin.test.BeforeTest
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@SpringBootTest(
    classes = [
        TicketCompletedEventJob::class,
        KafkaAutoConfiguration::class,
    ],
)
@EmbeddedKafka
class TicketCompletedEventJobTest
    @Autowired
    constructor(
        @SpyBean
        private val messageService: KafkaMessageService<String, TicketCompletedEventResponse>,
        @MockBean
        private val repo: TicketCompletedEventRepository,
        private val broker: EmbeddedKafkaBroker,
        private val job: TicketCompletedEventJob,
    ) {
        private lateinit var container: KafkaMessageListenerContainer<String, TicketCompletedEventResponse>

        @BeforeTest
        fun init() {
            container = createKafkaContainer(broker, KafkaTopic.TICKET_COMPLETED)
        }

        @AfterEach
        fun reset() {
            container.stop()
        }

        @ParameterizedTest
        @DisplayName("전송 실패한 티켓 완료 이벤트 재전송 테스트")
        @MethodSource("${EventTestParameters.PATH}#provideEvent")
        fun given_notSentTicketCompletedEvent_when_resendTicketCompletionEvent_then_resendMessage(event: TicketCompletedEvent) {
            val events = setOf(event)
            val queue = LinkedBlockingQueue<ConsumerRecord<String, TicketCompletedEventResponse>>()

            Mockito
                .`when`(repo.findByAuditCreatedAtBefore(mockAny()))
                .thenReturn(events)
            container.messageListener(broker) {
                queue.add(it)
            }

            job.resendTicketCompletedEvent()

            Thread.sleep(500)
            assertTrue(queue.isNotEmpty())
            for ((event, record) in events.zip(queue)) {
                val message = event.toMessage()

                assertEquals(message.key, record.key())
                assertEquals(message.payload?.id, record.value().id)
                assertEquals(message.payload?.behavior, record.value().behavior)
                assertEquals(message.payload?.target, record.value().target)
                assertEquals(message.payload?.color, record.value().color)
                assertEquals(message.payload?.swipeCount, record.value().swipeCount)
            }

            Mockito.verify(repo).deleteAllById(Mockito.argThat<Collection<Long>> { it.isNotEmpty() })
        }

        @ParameterizedTest
        @DisplayName("티켓 완료 이벤트 메시지 재전송 실패 테스트")
        @MethodSource("${EventTestParameters.PATH}#provideEvent")
        fun given_notSentTicketCompletedEvent_when_resendTicketCompletionEventFail_then_doNothing(event: TicketCompletedEvent) {
            val events = setOf(event)

            Mockito
                .`when`(repo.findByAuditCreatedAtBefore(mockAny()))
                .thenReturn(events)
            Mockito
                .`when`(messageService.send(Mockito.anyString(), Mockito.anyCollection()))
                .thenReturn(emptyList())

            job.resendTicketCompletedEvent()

            Mockito.verify(repo).deleteAllById(Mockito.argThat<Collection<Long>> { it.isEmpty() })
        }
    }

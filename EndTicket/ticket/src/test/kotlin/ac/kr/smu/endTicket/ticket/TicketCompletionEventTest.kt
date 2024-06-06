package ac.kr.smu.endTicket.ticket

import ac.kr.smu.endTicket.common.kafka.constant.KafkaTopic
import ac.kr.smu.endTicket.common.kafka.test.createKafkaContainer
import ac.kr.smu.endTicket.common.kafka.test.messageListener
import ac.kr.smu.endTicket.test.mockAny
import ac.kr.smu.endTicket.ticket.domain.model.Ticket
import ac.kr.smu.endTicket.ticket.domain.model.TicketCompletionEvent
import ac.kr.smu.endTicket.ticket.domain.repository.TicketCompletionEventRepository
import ac.kr.smu.endTicket.ticket.listener.TicketCompletionEventListener
import ac.kr.smu.endTicket.ticket.service.TicketCompletionEventMessageService
import ac.kr.smu.endTicket.ticket.service.TicketCompletionEventService
import ac.kr.smu.endTicket.ticket.ui.response.TicketResponse
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
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.TimeUnit
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@SpringBootTest(
    classes = [
        TicketCompletionEventService::class,
        TicketCompletionEventListener::class,
        TicketCompletionEventMessageService::class,
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
class TicketCompletionEventTest @Autowired constructor(
    @MockBean
    private val repo: TicketCompletionEventRepository,
    private val eventService: TicketCompletionEventService,
    private val broker: EmbeddedKafkaBroker,
    private val listener: TicketCompletionEventListener
) {

    private lateinit var container: KafkaMessageListenerContainer<String, TicketResponse>

    @Test
    @DisplayName("티켓 완료 이벤트 테스트")
    fun given_ticketCompletionEvent_then_saveEvent_and_sendMessage(){
        val ticket = Ticket.from(TICKET_REQUEST, USER_ID)
        val event = TicketCompletionEvent(ticket)
        val queue = LinkedBlockingQueue<ConsumerRecord<String, TicketResponse>>()

        container = createKafkaContainer(broker, KafkaTopic.TICKET_COMPLETION)
        container.messageListener(broker){
            queue.add(it)
        }

        eventService.eventPublish(event)

        val record = queue.poll(500, TimeUnit.MILLISECONDS)
        val message = event.toMessage()

        Mockito.verify(repo, Mockito.times(2)).save(mockAny())
        assertNotNull(record)
        assertEquals(message.payload, record.value())
        assertEquals(message.key, record.key().toLong())

        container.stop()
    }

}
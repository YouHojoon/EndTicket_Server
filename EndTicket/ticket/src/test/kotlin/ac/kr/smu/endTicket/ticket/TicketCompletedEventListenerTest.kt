package ac.kr.smu.endTicket.ticket

import KafkaMessageService
import ac.kr.smu.endticket.common.kafka.constant.KafkaTopic
import ac.kr.smu.endticket.common.kafka.test.createKafkaContainer
import ac.kr.smu.endticket.common.kafka.test.messageListener
import ac.kr.smu.endTicket.test.mockAny
import ac.kr.smu.endTicket.ticket.domain.model.Ticket
import ac.kr.smu.endTicket.ticket.domain.model.TicketCompletedEvent
import ac.kr.smu.endTicket.ticket.domain.repository.TicketCompletedEventRepository
import ac.kr.smu.endTicket.ticket.listener.TicketCompletedEventListener
import ac.kr.smu.endTicket.ticket.service.TicketCompletedEventService
import ac.kr.smu.endTicket.ticket.ui.response.TicketResponse
import org.apache.kafka.clients.consumer.ConsumerRecord
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
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@SpringBootTest(
    classes = [
        TicketCompletedEventService::class,
        TicketCompletedEventListener::class,
        KafkaAutoConfiguration::class
    ]
)
@EmbeddedKafka
class TicketCompletedEventListenerTest @Autowired constructor(
    @MockBean
    private val repo: TicketCompletedEventRepository,
    @SpyBean
    private val messageService: KafkaMessageService<String, TicketResponse>,
    private val eventService: TicketCompletedEventService,
    private val broker: EmbeddedKafkaBroker
) {
    private lateinit var container: KafkaMessageListenerContainer<String, TicketResponse>

    @BeforeTest
    fun init(){
        container = createKafkaContainer(broker, KafkaTopic.TICKET_COMPLETION)
    }
    @AfterTest
    fun reset(){
        container.stop()
    }

    @Test
    @DisplayName("티켓 완료 이벤트 테스트")
    fun given_ticketCompletionEvent_then_saveEvent_and_sendMessage(){
        val ticket = Ticket.from(TICKET_REQUEST, USER_ID)
        val event = TicketCompletedEvent(ticket)
        val queue = LinkedBlockingQueue<ConsumerRecord<String, TicketResponse>>()

        container.messageListener(broker){
            queue.add(it)
        }

        eventService.publishEvent(event)

        val record = queue.poll(500, TimeUnit.MILLISECONDS)
        val message = event.toMessage()

        Mockito.verify(repo, Mockito.times(2)).save(mockAny())
        assertNotNull(record)
        assertEquals(message.payload, record.value())
        assertEquals(message.key, record.key())
    }

    @Test
    @DisplayName("티켓 완료 이벤트 테스트")
    fun given_ticketCompletionEvent_when_sendMessageFail_then_doNothing(){
        val ticket = Ticket.from(TICKET_REQUEST, USER_ID)
        val event = TicketCompletedEvent(ticket)
        val mockEvent = Mockito.mock(TicketCompletedEvent::class.java)
        val message = event.toMessage()

        Mockito.`when`(mockEvent.toMessage())
            .thenReturn(message)
        Mockito.`when`(messageService.send(KafkaTopic.TICKET_COMPLETION, message))
            .thenReturn(CompletableFuture.failedFuture(RuntimeException()))

        eventService.publishEvent(mockEvent)

        Mockito.verify(repo, Mockito.only()).save(mockEvent)
    }
}
package ac.kr.smu.endTicket

import ac.kr.smu.endTicket.common.kafka.constant.KafkaTopic
import ac.kr.smu.endTicket.test.createKafkaContainer
import ac.kr.smu.endTicket.test.messageListener
import ac.kr.smu.endTicket.ticket.domain.model.Ticket
import ac.kr.smu.endTicket.ticket.domain.model.TicketCompletionEvent
import ac.kr.smu.endTicket.ticket.domain.repository.TicketCompletionEventRepository
import ac.kr.smu.endTicket.ticket.domain.service.TicketCompletionEventMessageService
import ac.kr.smu.endTicket.ticket.domain.service.TicketCompletionEventService
import ac.kr.smu.endTicket.ticket.infra.listener.TicketCompletionEventListener
import ac.kr.smu.endTicket.ticket.ui.response.TicketResponse
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

@SpringBootTest
@SpringJUnitConfig(classes = [
    TicketCompletionEventService::class,
    TicketCompletionEventListener::class,
    TicketCompletionEventMessageService::class,
    KafkaAutoConfiguration::class
])
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
        val event = TicketCompletionEvent.from(ticket)
        val queue = LinkedBlockingQueue<TicketResponse>()

        container = createKafkaContainer(broker, KafkaTopic.TICKET_COMPLETION)
        container.messageListener(broker){
            queue.add(it.value())
        }

        eventService.eventPublish(event)
        Mockito.verify(repo, Mockito.times(1)).save(event)

        val response = queue.poll(500, TimeUnit.MILLISECONDS)
        assertNotNull(response)
        assertEquals(TicketResponse.from(ticket), response)

        container.stop()
    }

}
package ac.kr.smu.endTicket

import ac.kr.smu.endTicket.constant.KafkaTopic
import ac.kr.smu.endTicket.ticket.domain.model.Ticket
import ac.kr.smu.endTicket.ticket.domain.model.TicketCompletionEvent
import ac.kr.smu.endTicket.ticket.domain.repository.TicketCompletionEventRepository
import ac.kr.smu.endTicket.ticket.domain.service.TicketCompletionEventMessageService
import ac.kr.smu.endTicket.ticket.domain.service.TicketCompletionEventService
import ac.kr.smu.endTicket.ticket.infra.listener.TicketCompletionEventListener
import ac.kr.smu.endTicket.ticket.ui.request.TicketRequest
import ac.kr.smu.endTicket.ticket.ui.response.TicketResponse
import org.apache.catalina.core.ApplicationContext
import org.apache.kafka.common.serialization.StringDeserializer
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.Mock
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory
import org.springframework.kafka.core.DefaultKafkaConsumerFactory
import org.springframework.kafka.listener.ContainerProperties
import org.springframework.kafka.listener.KafkaMessageListenerContainer
import org.springframework.kafka.listener.MessageListener
import org.springframework.kafka.support.serializer.JsonDeserializer
import org.springframework.kafka.test.EmbeddedKafkaBroker
import org.springframework.kafka.test.context.EmbeddedKafka
import org.springframework.kafka.test.utils.ContainerTestUtils
import org.springframework.kafka.test.utils.KafkaTestUtils
import org.springframework.test.context.TestExecutionListeners
import org.springframework.test.context.event.ApplicationEventsTestExecutionListener
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener
import java.util.concurrent.BlockingQueue
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
        createConsumer(queue)

        eventService.eventPublish(event)
        Mockito.verify(repo, Mockito.times(1)).save(event)

        val response = queue.poll(500, TimeUnit.MILLISECONDS)
        assertNotNull(response)
        assertEquals(TicketResponse.from(ticket), response)

        container.stop()
    }

    private fun createConsumer(queue: BlockingQueue<TicketResponse>){
        val config =
            KafkaTestUtils.consumerProps("test","false",broker)
        val deserializer = JsonDeserializer<TicketResponse>()
        deserializer.addTrustedPackages(TicketResponse::class.java.packageName)

        val consumerFactory = DefaultKafkaConsumerFactory(config, StringDeserializer(),deserializer)
        val listener = ConcurrentKafkaListenerContainerFactory<String, TicketResponse>()

        listener.consumerFactory = consumerFactory
        listener.createContainer(KafkaTopic.TICKET_COMPLETION)

        container = KafkaMessageListenerContainer(consumerFactory, ContainerProperties(KafkaTopic.TICKET_COMPLETION))
        container.setupMessageListener(
            MessageListener<String, TicketResponse> {
                queue.add(it.value())
            }
        )
        container.start()

        ContainerTestUtils.waitForAssignment(container, broker.partitionsPerTopic)
    }
}
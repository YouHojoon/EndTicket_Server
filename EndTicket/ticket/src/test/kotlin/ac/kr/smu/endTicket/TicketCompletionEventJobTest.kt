package ac.kr.smu.endTicket

import ac.kr.smu.endTicket.constant.KafkaTopic
import ac.kr.smu.endTicket.ticket.domain.job.TicketCompletionEventJob
import ac.kr.smu.endTicket.ticket.domain.model.Ticket
import ac.kr.smu.endTicket.ticket.domain.model.TicketCompletionEvent
import ac.kr.smu.endTicket.ticket.domain.repository.TicketCompletionEventRepository
import ac.kr.smu.endTicket.ticket.ui.request.TicketRequest
import ac.kr.smu.endTicket.ticket.ui.response.TicketResponse
import org.apache.kafka.common.serialization.StringDeserializer
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Autowired
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
import org.springframework.scheduling.annotation.EnableScheduling
import java.time.LocalDateTime
import java.util.concurrent.BlockingQueue
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.TimeUnit
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull


@SpringBootTest(
    properties = [
        "schedules.resend-ticket-completion-event.fixedDelay=200",
        "schedules.resend-ticket-completion-event.initialDelay=0"
    ],
)
@EmbeddedKafka(
    partitions = 3,
    brokerProperties = [
        "listeners=PLAINTEXT://localhost:9292"
    ],
    ports = [9292]
)
@EnableScheduling
class TicketCompletionEventJobTest @Autowired constructor(
    @MockBean
    private val repo: TicketCompletionEventRepository,
    private val broker: EmbeddedKafkaBroker,
    private val job: TicketCompletionEventJob

) {
    private lateinit var container: KafkaMessageListenerContainer<String, TicketResponse>

    @Suppress("UNCHECKED_CAST")
    private fun <T> any(): T {
        Mockito.any<T>()
        return  null as T
    }


    @Test
    @DisplayName("전송 실패한 티켓 완료 이벤트 재전송 테스트")
    fun after_fixedDelay_then_runResendTicketCompletionEvent(){
        val event = TicketCompletionEvent.from(
            Ticket.from(
                TicketRequest(
                    "b",
                    "t",
                    Ticket.Color.RED1,
                    Ticket.Type.HEALTH,
                    Ticket.MaxSwipeCount.TEN,
                ), 1L
            )
        )

        Mockito.`when`(
                repo.findByPublishedIsFalseAndCreateAtBefore(any())
            ).thenReturn(
                setOf(event)
            )

        val queue = LinkedBlockingQueue<TicketResponse>()
        createConsumer(queue)

        val message = queue.poll(500, TimeUnit.MILLISECONDS)
        assertNotNull(message)
        assertEquals(event.toResponse().payload, message)
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
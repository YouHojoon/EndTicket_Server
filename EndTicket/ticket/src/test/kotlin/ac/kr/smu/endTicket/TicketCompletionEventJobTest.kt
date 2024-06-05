package ac.kr.smu.endTicket

import ac.kr.smu.endTicket.common.kafka.test.createKafkaContainer
import ac.kr.smu.endTicket.common.kafka.test.messageListener
import ac.kr.smu.endTicket.common.kafka.constant.KafkaTopic
import ac.kr.smu.endTicket.ticket.domain.job.TicketCompletionEventJob
import ac.kr.smu.endTicket.ticket.domain.model.Ticket
import ac.kr.smu.endTicket.ticket.domain.model.TicketCompletionEvent
import ac.kr.smu.endTicket.ticket.domain.repository.TicketCompletionEventRepository
import ac.kr.smu.endTicket.ticket.domain.service.TicketCompletionEventMessageService
import ac.kr.smu.endTicket.ticket.ui.response.TicketResponse
import org.junit.jupiter.api.DisplayName
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.kafka.listener.KafkaMessageListenerContainer
import org.springframework.kafka.test.EmbeddedKafkaBroker
import org.springframework.kafka.test.context.EmbeddedKafka
import org.springframework.scheduling.annotation.SchedulingConfiguration
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig
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
@SpringJUnitConfig(classes = [
    TicketCompletionEventJob::class,
    KafkaAutoConfiguration::class,
    TicketCompletionEventMessageService::class,
    SchedulingConfiguration::class
])
class TicketCompletionEventJobTest @Autowired constructor(
    @MockBean
    private val repo: TicketCompletionEventRepository,
    private val broker: EmbeddedKafkaBroker,

) {
    private lateinit var container: KafkaMessageListenerContainer<String, TicketResponse>

    @Suppress("UNCHECKED_CAST")
    private fun <T> any(): T {
        Mockito.any<T>()
        return null as T
    }


    @Test
    @DisplayName("전송 실패한 티켓 완료 이벤트 재전송 테스트")
    fun after_fixedDelay_then_runResendTicketCompletionEvent(){
        val event = TicketCompletionEvent.from(
            Ticket.from(TICKET_REQUEST, USER_ID)
        )

        Mockito.`when`(
                repo.findByPublishedIsFalseAndCreateAtBefore(any())
            ).thenReturn(
                setOf(event)
            )

        val queue = LinkedBlockingQueue<TicketResponse>()

        container = createKafkaContainer(broker, KafkaTopic.TICKET_COMPLETION)
        container.messageListener(broker){
            queue.add(it.value())
        }

        val message = queue.poll(500, TimeUnit.MILLISECONDS)

        assertNotNull(message)
        assertEquals(event.toResponse().payload, message)

        container.stop()
    }


}
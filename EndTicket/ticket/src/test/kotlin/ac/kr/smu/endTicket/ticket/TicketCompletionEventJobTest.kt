package ac.kr.smu.endTicket.ticket

import ac.kr.smu.endTicket.common.kafka.constant.KafkaTopic
import ac.kr.smu.endTicket.common.kafka.test.createKafkaContainer
import ac.kr.smu.endTicket.common.kafka.test.messageListener
import ac.kr.smu.endTicket.test.mockAny
import ac.kr.smu.endTicket.ticket.job.TicketCompletionEventJob
import ac.kr.smu.endTicket.ticket.domain.model.Ticket
import ac.kr.smu.endTicket.ticket.domain.model.TicketCompletionEvent
import ac.kr.smu.endTicket.ticket.domain.repository.TicketCompletionEventRepository
import ac.kr.smu.endTicket.ticket.infra.config.KafkaConfig
import ac.kr.smu.endTicket.ticket.ui.response.TicketResponse
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.junit.jupiter.api.AfterEach
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
import java.util.concurrent.LinkedBlockingQueue
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue


@SpringBootTest(
    classes = [
        TicketCompletionEventJob::class,
        KafkaAutoConfiguration::class,
        KafkaConfig::class,
              ],
)
@EmbeddedKafka
class TicketCompletionEventJobTest @Autowired constructor(
    @MockBean
    private val repo: TicketCompletionEventRepository,
    private val broker: EmbeddedKafkaBroker,
    private val job: TicketCompletionEventJob
) {
    private lateinit var container: KafkaMessageListenerContainer<String, TicketResponse>
    @BeforeTest
    fun init(){
        container = createKafkaContainer(broker, KafkaTopic.TICKET_COMPLETION)
    }
    @AfterEach
    fun reset(){
        container.stop()
    }
    @Test
    @DisplayName("전송 실패한 티켓 완료 이벤트 재전송 테스트")
    fun after_fixedDelay_then_runResendTicketCompletionEvent(){
        val events = setOf(TicketCompletionEvent(Ticket.from(TICKET_REQUEST, USER_ID)))
        val queue = LinkedBlockingQueue<ConsumerRecord<String, TicketResponse>>()

        Mockito.`when`(repo.findByIsSentFalseAndAuditCreatedAtBefore(mockAny()))
            .thenReturn(events)
        container.messageListener(broker){
            queue.add(it)
        }

        job.resendTicketCompletionEvent()

        Thread.sleep(500)
        assertTrue(queue.isNotEmpty())
        for ((event, record) in events.zip(queue)){
            val message = event.toMessage()

            assertEquals(message.key, record.key())
            assertEquals(message.payload, record.value())
        }

        Mockito.verify(repo, Mockito.atLeast(1))
            .saveAll(Mockito.argThat<Collection<TicketCompletionEvent>> { it.isNotEmpty() })
    }


}
package ac.kr.smu.endticket.history

import ac.kr.smu.endticket.common.kafka.constant.KafkaTopic
import ac.kr.smu.endticket.common.kafka.test.createProducer
import ac.kr.smu.endticket.common.test.mockAny
import ac.kr.smu.endticket.history.domain.model.History
import ac.kr.smu.endticket.history.domain.model.TicketHistory
import ac.kr.smu.endticket.history.domain.repository.TicketHistoryRepository
import ac.kr.smu.endticket.history.infra.messaging.TicketCompletedEventResponse
import ac.kr.smu.endticket.history.service.TicketCompletedEventConsumeService
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.clients.producer.ProducerRecord
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.ArgumentCaptor
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.kafka.support.Acknowledgment
import org.springframework.kafka.test.EmbeddedKafkaBroker
import org.springframework.kafka.test.context.EmbeddedKafka

@SpringBootTest(
    classes = [
        TicketCompletedEventConsumeService::class,
        KafkaAutoConfiguration::class
    ]
)
@EmbeddedKafka(partitions = 3)
class TicketCompletedEventConsumeServiceTest @Autowired constructor(
    @MockBean
    private val repo: TicketHistoryRepository,
    private val broker: EmbeddedKafkaBroker,
    private val service: TicketCompletedEventConsumeService
){
    private val producer = createProducer<TicketCompletedEventResponse>(broker)

    @Test
    @DisplayName("티켓 완료 이벤트 수신 테스트")
    fun given_ticketCompletedEvent_when_consume_then_saveTicketHistory(){
        Mockito.`when`(repo.existsById(TICKET_COMPLETED_EVENT_RESPONSE.id))
            .thenReturn(false)

        producer.send(ProducerRecord(KafkaTopic.TICKET_COMPLETION, USER_ID.toString(), TICKET_COMPLETED_EVENT_RESPONSE))

        Thread.sleep(500L)

        Mockito.verify(repo).save(mockAny())
        Mockito.verify(repo).existsBySpecificIdAndType(Mockito.anyLong(), mockAny())
    }

    @Test
    @DisplayName("티켓 완료 이벤트 수신 실패 테스트")
    fun given_ticketCompletedEvent_when_consumeFail_then_sendNack(){
        val record = Mockito.mock(ConsumerRecord::class.java) as ConsumerRecord<String, TicketCompletedEventResponse>
        val ack = Mockito.mock(Acknowledgment::class.java)

        Mockito.`when`(record.topic()).thenReturn(KafkaTopic.TICKET_COMPLETION)
        Mockito.`when`(record.value()).thenReturn(TICKET_COMPLETED_EVENT_RESPONSE)
        Mockito.`when`(repo.existsBySpecificIdAndType(Mockito.anyLong(), mockAny())).thenThrow(RuntimeException())

        service.consume(record,ack)

        Mockito.verify(ack).nack(mockAny())
    }

    @Test
    @DisplayName("티켓 완료 이벤트 중복 처리 테스트")
    fun given_ticketCompletedEventAlreadyConsumed_when_consume_then_ack(){
        val record = Mockito.mock(ConsumerRecord::class.java) as ConsumerRecord<String, TicketCompletedEventResponse>
        val ack = Mockito.mock(Acknowledgment::class.java)

        Mockito.`when`(record.topic()).thenReturn(KafkaTopic.TICKET_COMPLETION)
        Mockito.`when`(record.value()).thenReturn(
            TICKET_COMPLETED_EVENT_RESPONSE
        )
        Mockito.`when`(repo.existsBySpecificIdAndType(Mockito.anyLong(), mockAny())).thenReturn(true)
        service.consume(record,ack)

        Mockito.verify(repo, Mockito.only()).existsBySpecificIdAndType(TICKET_COMPLETED_EVENT_RESPONSE.id, TicketHistory::class)
        Mockito.verify(ack).acknowledge()
    }
}
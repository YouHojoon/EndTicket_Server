package ac.kr.smu.endTicket.futureMe

import ac.kr.smu.endTicket.common.kafka.constant.KafkaTopic
import ac.kr.smu.endTicket.common.kafka.test.createProducer
import ac.kr.smu.endTicket.futureMe.domain.event.repository.EventRepository
import ac.kr.smu.endTicket.futureMe.domain.event.model.TicketCompletedEvent
import ac.kr.smu.endTicket.futureMe.futureMe.USER_ID
import ac.kr.smu.endTicket.futureMe.infra.config.KafkaConfig
import ac.kr.smu.endTicket.futureMe.service.FutureMeService
import ac.kr.smu.endTicket.futureMe.infra.messaging.TicketCompletedEventResponse
import ac.kr.smu.endTicket.futureMe.service.TicketCompletedEventConsumeService
import ac.kr.smu.endTicket.test.mockAny
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.clients.producer.ProducerRecord
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.kafka.support.Acknowledgment
import org.springframework.kafka.test.EmbeddedKafkaBroker
import org.springframework.kafka.test.context.EmbeddedKafka


@SpringBootTest(classes = [
    TicketCompletedEventConsumeService::class,
    KafkaAutoConfiguration::class,
    KafkaConfig::class
])
@EmbeddedKafka(partitions = 3)
class TicketCompletedEventConsumeServiceTest @Autowired constructor(
    @MockBean
    private val repo: EventRepository,
    @MockBean
    private val futureMeService: FutureMeService,
    private val broker: EmbeddedKafkaBroker,
    private val service: TicketCompletedEventConsumeService
) {

    @Test
    @DisplayName("티켓 완료 이벤트 처리 테스트")
    fun given_ticketCompletedEvent_when_consume_then_gainExperiencePoints_and_saveEvent(){
        val producer = createProducer<TicketCompletedEventResponse>(broker)
        val eventResponse = TicketCompletedEventResponse(1L)
        val record = ProducerRecord(KafkaTopic.TICKET_COMPLETION, USER_ID.toString(), eventResponse)

        Mockito
            .`when`(repo.existsByEventIDAndType(eventResponse.id, "TicketCompletionEvent"))
            .thenReturn(false)
        producer.send(record)
        Thread.sleep(1000)
        
        Mockito.verify(repo).save(mockAny<TicketCompletedEvent>())
        Mockito.verify(futureMeService).gainExperiencePoints(mockAny<TicketCompletedEvent>())
    }

    @Test
    @DisplayName("티켓 완료 이벤트 중복 처리 테스트")
    fun given_ticketCompletedEventAlreadyConsumed_when_consume_then_ack(){
        val record = Mockito.mock(ConsumerRecord::class.java) as ConsumerRecord<String, TicketCompletedEventResponse>
        val ack = Mockito.mock(Acknowledgment::class.java)

        Mockito.`when`(record.value()).thenReturn(
            TicketCompletedEventResponse(1L)
        )
        Mockito
            .`when`(repo.existsByEventIDAndType(1L, "TicketCompletionEvent"))
            .thenReturn(true)

        service.consume(record,ack)

        Mockito.verify(repo, Mockito.only()).existsByEventIDAndType(1L, "TicketCompletionEvent")
        Mockito.verify(ack).acknowledge()
    }

    @Test
    @DisplayName("티켓 완료 이벤트 처리 실패 테스트")
    fun given_ticketCompletionEvent_when_consumeFail_then_sendNack(){
        val ack = Mockito.mock(Acknowledgment::class.java)
        val record = Mockito.mock(ConsumerRecord::class.java) as ConsumerRecord<String, TicketCompletedEventResponse>

        Mockito.`when`(record.value()).thenReturn(
            TicketCompletedEventResponse(1L)
        )
        Mockito.`when`(repo.existsByEventIDAndType(Mockito.anyLong(), Mockito.anyString()))
            .thenThrow(RuntimeException())

        service.consume(record, ack)
        Mockito.verify(ack).nack(mockAny())
    }

}
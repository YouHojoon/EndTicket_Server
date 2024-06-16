package ac.kr.smu.endTicket.futureMe

import ac.kr.smu.endTicket.common.kafka.annotation.EnableAutoKafkaConfig
import ac.kr.smu.endTicket.common.kafka.constant.KafkaTopic
import ac.kr.smu.endTicket.common.kafka.test.createProducer
import ac.kr.smu.endTicket.futureMe.domain.event.repository.EventRepository
import ac.kr.smu.endTicket.futureMe.domain.event.model.TicketCompletionEvent
import ac.kr.smu.endTicket.futureMe.futureMe.USER_ID
import ac.kr.smu.endTicket.futureMe.infra.config.KafkaConfig
import ac.kr.smu.endTicket.futureMe.infra.messaging.ImaginationCompletionEventResponse
import ac.kr.smu.endTicket.futureMe.service.FutureMeService
import ac.kr.smu.endTicket.futureMe.infra.messaging.TicketCompletionEventResponse
import ac.kr.smu.endTicket.futureMe.service.TicketCompletionEventConsumeService
import ac.kr.smu.endTicket.test.mockAny
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.clients.producer.Producer
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.StringSerializer
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.kafka.core.DefaultKafkaProducerFactory
import org.springframework.kafka.support.Acknowledgment
import org.springframework.kafka.support.serializer.JsonSerializer
import org.springframework.kafka.test.EmbeddedKafkaBroker
import org.springframework.kafka.test.context.EmbeddedKafka
import org.springframework.kafka.test.utils.KafkaTestUtils


@SpringBootTest(classes = [
    TicketCompletionEventConsumeService::class,
    KafkaAutoConfiguration::class,
    KafkaConfig::class
])
@EmbeddedKafka(partitions = 3)
class TicketCompletionEventConsumeServiceTest @Autowired constructor(
    @MockBean
    private val repo: EventRepository,
    @MockBean
    private val futureMeService: FutureMeService,
    private val broker: EmbeddedKafkaBroker,
    private val service: TicketCompletionEventConsumeService
) {

    @Test
    @DisplayName("티켓 완료 이벤트 처리 테스트")
    fun given_ticketCompletionEvent_when_consume_then_gainExperiencePoints_and_saveEvent(){
        val producer = createProducer<TicketCompletionEventResponse>(broker)
        val eventResponse = TicketCompletionEventResponse(1L)
        val record = ProducerRecord(KafkaTopic.TICKET_COMPLETION, USER_ID.toString(), eventResponse)

        Mockito
            .`when`(repo.existsByEventIDAndType(eventResponse.id, "TicketCompletionEvent"))
            .thenReturn(false)
        producer.send(record)
        Thread.sleep(1000)
        
        Mockito.verify(repo).save(mockAny<TicketCompletionEvent>())
        Mockito.verify(futureMeService).gainExperiencePoints(mockAny<TicketCompletionEvent>())
    }

    @Test
    @DisplayName("티켓 완료 이벤트 중복 처리 테스트")
    fun given_ticketCompletionEventAlreadyConsumed_when_consume_then_ack(){
        val record = Mockito.mock(ConsumerRecord::class.java) as ConsumerRecord<String, TicketCompletionEventResponse>
        val ack = Mockito.mock(Acknowledgment::class.java)

        Mockito.`when`(record.value()).thenReturn(
            TicketCompletionEventResponse(1L)
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
        val record = Mockito.mock(ConsumerRecord::class.java) as ConsumerRecord<String, TicketCompletionEventResponse>

        Mockito.`when`(record.value()).thenReturn(
            TicketCompletionEventResponse(1L)
        )
        Mockito.`when`(repo.existsByEventIDAndType(Mockito.anyLong(), Mockito.anyString()))
            .thenThrow(RuntimeException())

        service.consume(record, ack)
        Mockito.verify(ack).nack(mockAny())
    }

}
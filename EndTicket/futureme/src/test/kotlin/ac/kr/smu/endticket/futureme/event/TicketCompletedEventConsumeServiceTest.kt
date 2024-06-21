package ac.kr.smu.endticket.futureme.event

import ac.kr.smu.endticket.common.kafka.constant.KafkaTopic
import ac.kr.smu.endticket.common.kafka.test.createProducer
import ac.kr.smu.endticket.common.test.mockAny
import ac.kr.smu.endticket.futureme.domain.event.model.TicketCompletedEvent
import ac.kr.smu.endticket.futureme.domain.event.repository.EventRepository
import ac.kr.smu.endticket.futureme.imagination.USER_ID
import ac.kr.smu.endticket.futureme.infra.config.KafkaConfig
import ac.kr.smu.endticket.futureme.infra.messaging.TicketCompletedEventResponse
import ac.kr.smu.endticket.futureme.service.FutureMeService
import ac.kr.smu.endticket.futureme.service.TicketCompletedEventConsumeService
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.clients.producer.ProducerRecord
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
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
            .`when`(repo.existsBySpecificIdAndType(eventResponse.id, TicketCompletedEvent::class))
            .thenReturn(false)
        producer.send(record)
        Thread.sleep(1000)
        
        Mockito.verify(repo).save(mockAny<TicketCompletedEvent>())
        Mockito.verify(futureMeService).gainExperiencePoints(mockAny<TicketCompletedEvent>())
    }

    @ParameterizedTest
    @DisplayName("티켓 완료 이벤트 중복 처리 테스트")
    @MethodSource("${EventTestParameters.PATH}#provideTicketCompletedEventRecordAndAck")
    fun given_ticketCompletedEventAlreadyConsumed_when_consume_then_ack(record: ConsumerRecord<String, TicketCompletedEventResponse>, ack: Acknowledgment){
        Mockito
            .`when`(repo.existsBySpecificIdAndType(record.value().id, TicketCompletedEvent::class))
            .thenReturn(true)

        service.consume(record,ack)

        Mockito.verify(repo, Mockito.only()).existsBySpecificIdAndType(1L, TicketCompletedEvent::class)
        Mockito.verify(ack).acknowledge()
    }

    @ParameterizedTest
    @DisplayName("티켓 완료 이벤트 처리 실패 테스트")
    @MethodSource("${EventTestParameters.PATH}#provideTicketCompletedEventRecordAndAck")
    fun given_ticketCompletionEvent_when_consumeFail_then_sendNack(record: ConsumerRecord<String, TicketCompletedEventResponse>, ack: Acknowledgment){
        Mockito.`when`(repo.existsBySpecificIdAndType(record.value().id, TicketCompletedEvent::class))
            .thenThrow(RuntimeException())

        service.consume(record, ack)
        Mockito.verify(ack).nack(mockAny())
    }

}
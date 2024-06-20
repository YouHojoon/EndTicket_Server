package ac.kr.smu.endticket.history

import ac.kr.smu.endticket.common.kafka.constant.KafkaTopic
import ac.kr.smu.endticket.common.kafka.test.createProducer
import ac.kr.smu.endticket.common.test.mockAny
import ac.kr.smu.endticket.history.domain.model.History
import ac.kr.smu.endticket.history.domain.model.ImaginationHistory
import ac.kr.smu.endticket.history.domain.model.TicketHistory
import ac.kr.smu.endticket.history.domain.repository.HistoryRepository
import ac.kr.smu.endticket.history.infra.messaging.EventResponse
import ac.kr.smu.endticket.history.infra.messaging.ImaginationCompletedEventResponse
import ac.kr.smu.endticket.history.infra.messaging.TicketCompletedEventResponse
import ac.kr.smu.endticket.history.service.EventConsumeService
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
import kotlin.reflect.KClass

@SpringBootTest(
    classes = [
        EventConsumeService::class,
        KafkaAutoConfiguration::class
    ]
)
@EmbeddedKafka(partitions = 3)
class EventConsumeServiceTest @Autowired constructor(
    @MockBean
    private val repo: HistoryRepository,
    private val broker: EmbeddedKafkaBroker,
    private val service: EventConsumeService
){
    private val producer = createProducer<EventResponse>(broker)

    @ParameterizedTest
    @DisplayName("완료 이벤트 수신 테스트")
    @MethodSource("ac.kr.smu.endticket.history.HistoryTestParameters#provideTopicAndResponseAndType")
    fun given_ticketCompletedEvent_when_consume_then_saveTicketHistory(topic: String, response: EventResponse, type: KClass<out History>){
        Mockito.`when`(repo.existsBySpecificIdAndType(response.id, type))
            .thenReturn(false)

        producer.send(ProducerRecord(topic, HistoryTestParameters.USER_ID.toString(), response))

        Thread.sleep(500L)
        Mockito.verify(repo).save(Mockito.argThat { it::class == type })
        Mockito.verify(repo).existsBySpecificIdAndType(response.id, type)
    }

    @ParameterizedTest
    @DisplayName("완료 이벤트 수신 실패 테스트")
    @MethodSource("ac.kr.smu.endticket.history.HistoryTestParameters#provideRecord")
    fun given_ticketCompletedEvent_when_consumeFail_then_sendNack(record: ConsumerRecord<String,out EventResponse>){
        val ack = Mockito.mock(Acknowledgment::class.java)

        Mockito.`when`(repo.existsBySpecificIdAndType(Mockito.anyLong(), mockAny())).thenThrow(RuntimeException())

        service.consume(record, ack)

        Mockito.verify(ack).nack(mockAny())
    }

    @ParameterizedTest
    @DisplayName("완료 이벤트 중복 처리 테스트")
    @MethodSource("ac.kr.smu.endticket.history.HistoryTestParameters#provideRecordAndType")
    fun given_ticketCompletedEventAlreadyConsumed_when_consume_then_ack(record: ConsumerRecord<String, out EventResponse>, type: KClass<out History>){
        val ack = Mockito.mock(Acknowledgment::class.java)
        Mockito.`when`(repo.existsBySpecificIdAndType(Mockito.anyLong(), mockAny())).thenReturn(true)

        service.consume(record, ack)

        Mockito.verify(repo).existsBySpecificIdAndType(record.value().id, type)
        Mockito.verify(ack).acknowledge()
    }

}
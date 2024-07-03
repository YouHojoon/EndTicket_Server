package ac.kr.smu.endticket.history

import ac.kr.smu.endticket.common.kafka.test.createProducer
import ac.kr.smu.endticket.common.test.mockAny
import ac.kr.smu.endticket.history.domain.model.History
import ac.kr.smu.endticket.history.domain.repository.HistoryRepository
import ac.kr.smu.endticket.history.infra.messaging.EventResponse
import ac.kr.smu.endticket.history.infra.messaging.TicketCompletedEventResponse
import ac.kr.smu.endticket.history.service.EventConsumeService
import ac.kr.smu.endticket.history.service.HistoryService
import ac.kr.smu.endticket.history.ui.response.HistoryCount
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.clients.producer.ProducerRecord
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.core.ValueOperations
import org.springframework.kafka.support.Acknowledgment
import org.springframework.kafka.test.EmbeddedKafkaBroker
import org.springframework.kafka.test.context.EmbeddedKafka
import java.lang.RuntimeException
import kotlin.test.BeforeTest

@SpringBootTest(
    classes = [
        EventConsumeService::class,
        KafkaAutoConfiguration::class
    ]
)
@EmbeddedKafka(partitions = 3)
class EventConsumeServiceTest @Autowired constructor(
    @MockBean
    private val historyService: HistoryService,
    private val broker: EmbeddedKafkaBroker,
    private val service: EventConsumeService
){
    private val producer = createProducer<EventResponse>(broker)
    @ParameterizedTest
    @DisplayName("완료 이벤트 수신 테스트")
    @MethodSource("${HistoryTestParameters.PATH}#provideTopicAndResponseAndType")
    fun given_event_when_consume_then_saveHistory(topic: String, response: EventResponse, type: History.Type){

        producer.send(ProducerRecord(topic, HistoryTestParameters.USER_ID.toString(), response))
        Thread.sleep(500L)

        Mockito.verify(historyService).saveHistory(mockAny(),Mockito.eq(HistoryTestParameters.USER_ID))
    }


    @ParameterizedTest
    @DisplayName("완료 이벤트 수신 실패 테스트")
    @MethodSource("${HistoryTestParameters.PATH}#provideRecord")
    fun given_event_when_consumeFail_then_sendNack(record: ConsumerRecord<String,out EventResponse>){
        val ack = Mockito.mock(Acknowledgment::class.java)
        Mockito.`when`(record.value())
            .thenThrow(RuntimeException())

        service.consume(record, ack)

        Mockito.verify(ack).nack(mockAny())
    }
}
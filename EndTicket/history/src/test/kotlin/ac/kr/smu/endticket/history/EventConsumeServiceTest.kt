package ac.kr.smu.endticket.history

import ac.kr.smu.endticket.common.kafka.test.createProducer
import ac.kr.smu.endticket.common.test.mockAny
import ac.kr.smu.endticket.history.domain.model.History
import ac.kr.smu.endticket.history.domain.repository.HistoryRepository
import ac.kr.smu.endticket.history.infra.messaging.EventResponse
import ac.kr.smu.endticket.history.service.EventConsumeService
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
    private val ops: ValueOperations<String,Any>,
    @MockBean
    private val repo: HistoryRepository,
    @MockBean
    private val redisTemplate: RedisTemplate<String,Any>,
    private val broker: EmbeddedKafkaBroker,
    private val service: EventConsumeService
){
    private val producer = createProducer<EventResponse>(broker)
    @BeforeTest
    fun init(){
        Mockito.`when`(redisTemplate.opsForValue()).thenReturn(ops)
    }
    @ParameterizedTest
    @DisplayName("완료 이벤트 수신 테스트")
    @MethodSource("${HistoryTestParameters.PATH}#provideTopicAndResponseAndType")
    fun given_event_when_consume_then_saveHistory(topic: String, response: EventResponse, type: History.Type){
        val key = "history-count::${HistoryTestParameters.USER_ID}"

        Mockito.`when`(repo.existsBySpecificIdAndType(response.id, type))
            .thenReturn(false)
        Mockito.`when`(ops.get(key)).thenReturn(null)

        producer.send(ProducerRecord(topic, HistoryTestParameters.USER_ID.toString(), response))

        Thread.sleep(500L)

        Mockito.verify(repo).save(mockAny())
        Mockito.verify(ops).get(key)
        Mockito.verify(repo).existsBySpecificIdAndType(response.id, type)
    }

    @ParameterizedTest
    @DisplayName("완료 이벤트 수신 시 기록 개수 증가 테스트")
    @MethodSource("${HistoryTestParameters.PATH}#provideTopicAndResponseAndType")
    fun given_eventAndHistoryCountInRedis_when_consume_then_saveHistoryAndUpdateHistoryCount(topic: String, response: EventResponse, type: History.Type){
        val key = "history-count::${HistoryTestParameters.USER_ID}"

        Mockito.`when`(repo.existsBySpecificIdAndType(response.id, type))
            .thenReturn(false)
        Mockito.`when`(ops.get(key))
            .thenReturn(HistoryCount(0,0))

        producer.send(ProducerRecord(topic, HistoryTestParameters.USER_ID.toString(), response))

        Thread.sleep(500L)

        Mockito.verify(repo).save(mockAny())
        Mockito.verify(ops).set(key, when(type){
            History.Type.TICKET -> HistoryCount(1,0)
            History.Type.IMAGINATION -> HistoryCount(0,1)
        })
        Mockito.verify(repo).existsBySpecificIdAndType(response.id, type)
    }

    @ParameterizedTest
    @DisplayName("완료 이벤트 수신 실패 테스트")
    @MethodSource("${HistoryTestParameters.PATH}#provideRecord")
    fun given_event_when_consumeFail_then_sendNack(record: ConsumerRecord<String,out EventResponse>){
        val ack = Mockito.mock(Acknowledgment::class.java)

        Mockito.`when`(repo.existsBySpecificIdAndType(Mockito.anyLong(), mockAny())).thenThrow(RuntimeException())

        service.consume(record, ack)

        Mockito.verify(ack).nack(mockAny())
    }

    @ParameterizedTest
    @DisplayName("완료 이벤트 중복 처리 테스트")
    @MethodSource("${HistoryTestParameters.PATH}#provideRecordAndType")
    fun given_eventAlreadyConsumed_when_consume_then_ack(record: ConsumerRecord<String, out EventResponse>, type: History.Type){
        val ack = Mockito.mock(Acknowledgment::class.java)
        Mockito.`when`(repo.existsBySpecificIdAndType(Mockito.anyLong(), mockAny())).thenReturn(true)

        service.consume(record, ack)

        Mockito.verify(repo).existsBySpecificIdAndType(record.value().id, type)
        Mockito.verify(ack).acknowledge()
    }

}
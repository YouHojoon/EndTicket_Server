package ac.kr.smu.endticket.history

import ac.kr.smu.endticket.common.kafka.constant.KafkaTopic
import ac.kr.smu.endticket.common.kafka.test.createProducer
import ac.kr.smu.endticket.common.test.mockAny
import ac.kr.smu.endticket.history.domain.model.History
import ac.kr.smu.endticket.history.domain.model.ImaginationHistory
import ac.kr.smu.endticket.history.domain.model.TicketHistory
import ac.kr.smu.endticket.history.domain.repository.TicketHistoryRepository
import ac.kr.smu.endticket.history.infra.messaging.EventResponse
import ac.kr.smu.endticket.history.infra.messaging.ImaginationCompletedEventResponse
import ac.kr.smu.endticket.history.infra.messaging.TicketCompletedEventResponse
import ac.kr.smu.endticket.history.service.EventConsumeService
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
    private val repo: TicketHistoryRepository,
    private val broker: EmbeddedKafkaBroker,
    private val service: EventConsumeService
){
    private val producer = createProducer<EventResponse>(broker)

    @Test
    @DisplayName("완료 이벤트 수신 테스트")
    fun given_ticketCompletedEvent_when_consume_then_saveTicketHistory(){
        val n = EventResponse::class.sealedSubclasses.size

        for(responseType in EventResponse::class.sealedSubclasses){
            val (topic, value) = topicAndValueOfResponse(responseType)

            Mockito.`when`(repo.existsBySpecificIdAndType(value.id, historyTypeOfResponse(responseType)))
                .thenReturn(false)

            producer.send(ProducerRecord(topic, USER_ID.toString(), value))
        }

        Thread.sleep(500L)
        Mockito.verify(repo, Mockito.times(n)).save(mockAny())
        Mockito.verify(repo, Mockito.times(n)).existsBySpecificIdAndType(Mockito.anyLong(), mockAny())
    }

    @Test
    @DisplayName("완료 이벤트 수신 실패 테스트")
    fun given_ticketCompletedEvent_when_consumeFail_then_sendNack(){
        val n = EventResponse::class.sealedSubclasses.size
        val ack = Mockito.mock(Acknowledgment::class.java)

        Mockito.`when`(repo.existsBySpecificIdAndType(Mockito.anyLong(), mockAny())).thenThrow(RuntimeException())

        for(responseType in EventResponse::class.sealedSubclasses){
            val record = mockRecord(responseType)

            service.consume(record,ack)
        }

        Mockito.verify(ack, Mockito.times(n)).nack(mockAny())
    }

    @Test
    @DisplayName("완료 이벤트 중복 처리 테스트")
    fun given_ticketCompletedEventAlreadyConsumed_when_consume_then_ack(){
        val n = EventResponse::class.sealedSubclasses.size
        val ack = Mockito.mock(Acknowledgment::class.java)

        Mockito.`when`(repo.existsBySpecificIdAndType(Mockito.anyLong(), mockAny())).thenReturn(true)

        for(responseType in EventResponse::class.sealedSubclasses){
            val record = mockRecord(responseType)

            service.consume(record,ack)

            Mockito.verify(repo).existsBySpecificIdAndType(record.value().id, historyTypeOfResponse(responseType))
        }

        Mockito.verify(ack, Mockito.times(n)).acknowledge()
    }

    /**
     * 이벤트 응답 타입에 맞는 카프카 토픽과 이벤트 응답을 반환하는 메소드
     * @param responseType 이벤트 응답
     * @return 카프카 토픽과 이벤트 응답
     */
    private fun topicAndValueOfResponse(responseType: KClass<out EventResponse>) = when(responseType){
        TicketCompletedEventResponse::class -> KafkaTopic.TICKET_COMPLETION to TICKET_COMPLETED_EVENT_RESPONSE
        ImaginationCompletedEventResponse::class ->  KafkaTopic.IMAGINATION_COMPLETION to IMAGINATION_COMPLETED_EVENT_RESPONSE
        else -> throw IllegalArgumentException("$responseType 은 지원하지 않는 타입입니다.")
    }

    /**
     * ConsumerRecord를 mocking 하는 메소드
     * @param responseType mocking할 EventResponse 타입
     * @return mocking된 ConsumerRecord
     */
    private fun mockRecord(responseType: KClass<out EventResponse>): ConsumerRecord<String, EventResponse>{
        val record = Mockito.mock(ConsumerRecord::class.java) as ConsumerRecord<String, EventResponse>
        val (topic,value) = topicAndValueOfResponse(responseType)

        Mockito.`when`(record.key()).thenReturn(USER_ID.toString())
        Mockito.`when`(record.topic()).thenReturn(topic)
        Mockito.`when`(record.value()).thenReturn(value)

        return record
    }

    /**
     * 이벤트 응답에 맞는 기록 type을 반환하는 메소드
     * @param responseType 이벤트 응답
     * @return 기록 type
     */
    private fun historyTypeOfResponse(responseType: KClass<out EventResponse>): KClass<out History> = when(responseType){
        TicketCompletedEventResponse::class -> TicketHistory::class
        ImaginationCompletedEventResponse::class -> ImaginationHistory::class
        else -> throw IllegalArgumentException("$responseType 은 지원하지 않는 타입입니다.")
    }
}
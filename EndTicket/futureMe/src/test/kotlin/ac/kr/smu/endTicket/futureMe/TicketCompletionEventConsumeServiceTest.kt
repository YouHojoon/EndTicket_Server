package ac.kr.smu.endTicket.futureMe

import ac.kr.smu.endTicket.constant.KafkaTopic
import ac.kr.smu.endTicket.futureMe.domain.event.EventRepository
import ac.kr.smu.endTicket.futureMe.domain.event.TicketCompletionEvent
import ac.kr.smu.endTicket.futureMe.service.FutureMeService
import ac.kr.smu.endTicket.futureMe.ui.response.TicketResponse
import org.apache.kafka.clients.producer.Producer
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.LongSerializer
import org.apache.kafka.common.serialization.StringSerializer
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.kafka.core.DefaultKafkaProducerFactory
import org.springframework.kafka.support.serializer.JsonSerializer
import org.springframework.kafka.test.EmbeddedKafkaBroker
import org.springframework.kafka.test.context.EmbeddedKafka
import org.springframework.kafka.test.utils.KafkaTestUtils


@SpringBootTest
//@SpringJUnitConfig(classes = [
//    TicketCompletionEventConsumeService::class,
//    KafkaAutoConfiguration::class
//])
//@Import(KafkaConfig::class)
@EmbeddedKafka(
    partitions = 3,
    ports = [9292],
    topics = [KafkaTopic.TICKET_COMPLETION],
    brokerProperties = [
        "listeners=PLAINTEXT://localhost:9292"
    ]
)
class TicketCompletionEventConsumeServiceTest @Autowired constructor(
    @MockBean
    private val repo: EventRepository,
    @MockBean
    private val futureMeService: FutureMeService,
    private val broker: EmbeddedKafkaBroker
) {

    @Test
    @DisplayName("티켓 완료 이벤트 처리 테스트")
    fun given_ticketCompletionEvent_when_consume_then_gainExperiencePoints_and_saveEvent(){
        val producer = createProducer()
        val ticketResponse = TicketResponse(1L)
        val record = ProducerRecord(KafkaTopic.TICKET_COMPLETION, USER_ID, ticketResponse)

        Mockito.`when`(repo.findByEventIDAndType(ticketResponse.id, "TicketCompletionEvent"))
            .thenReturn(null)
        producer.send(record)
        Thread.sleep(3000)

        Mockito.verify(repo).save(any())
        Mockito.verify(futureMeService).gainExperiencePoints(any())
    }

    private fun createProducer(): Producer<Long, TicketResponse>{
        val properties = KafkaTestUtils.producerProps(broker)
        return DefaultKafkaProducerFactory(properties, LongSerializer(), JsonSerializer<TicketResponse>()).createProducer()
    }

    @Suppress("UNCHECKED_CAST")
    private fun <T> any(): T {
        Mockito.any<T>()
        return null as T
    }
}
package ac.kr.smu.endticket.ticket.event

import ac.kr.smu.endticket.common.kafka.constant.KafkaTopic
import ac.kr.smu.endticket.common.kafka.test.createProducer
import ac.kr.smu.endticket.common.test.mockAny
import ac.kr.smu.endticket.ticket.service.TicketService
import ac.kr.smu.endticket.ticket.service.UserDeletedEventConsumeService
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

@SpringBootTest(
    classes = [
        UserDeletedEventConsumeService::class,
        KafkaAutoConfiguration::class,
    ],
)
@EmbeddedKafka
class UserDeletedEventConsumeServiceTest
    @Autowired
    constructor(
        @MockBean
        private val ticketService: TicketService,
        private val service: UserDeletedEventConsumeService,
        private val broker: EmbeddedKafkaBroker,
    ) {
        @Test
        @DisplayName("회원 탈퇴 이벤트 테스트")
        fun given_userDeletedEvent_whenConsume_then_deleteAllTicketOfUser() {
            val producer = createProducer<Void>(broker)

            producer.send(ProducerRecord(KafkaTopic.USER_DELETED, EventTestParameters.USER_ID.toString(), null))
            Thread.sleep(1000L)

            Mockito.verify(ticketService).deleteByUserId(EventTestParameters.USER_ID)
        }

        @Test
        @DisplayName("이벤트 수신 실패 테스트")
        fun given_userDeletedEvent_whenConsumeFail_then_sendNack() {
            val record = Mockito.mock(ConsumerRecord::class.java) as ConsumerRecord<String, Void>
            val ack = Mockito.mock(Acknowledgment::class.java)

            Mockito.`when`(record.key()).thenThrow(RuntimeException())

            service.consume(record, ack)

            Mockito.verify(ack).nack(mockAny())
        }
    }

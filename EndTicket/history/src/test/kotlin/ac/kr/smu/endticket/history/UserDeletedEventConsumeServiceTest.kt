package ac.kr.smu.endticket.history

import ac.kr.smu.endticket.common.kafka.constant.KafkaTopic
import ac.kr.smu.endticket.common.kafka.test.createProducer
import ac.kr.smu.endticket.common.test.mockAny
import ac.kr.smu.endticket.history.service.HistoryService
import ac.kr.smu.endticket.history.service.UserDeletedEventConsumeService
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
import java.lang.RuntimeException

@SpringBootTest(
    classes = [
        KafkaAutoConfiguration::class,
        UserDeletedEventConsumeService::class,
        HistoryService::class,
    ],
)
@EmbeddedKafka
class UserDeletedEventConsumeServiceTest
    @Autowired
    constructor(
        @MockBean
        private val historyService: HistoryService,
        private val service: UserDeletedEventConsumeService,
        private val broker: EmbeddedKafkaBroker,
    ) {
        @Test
        @DisplayName("회원 탈퇴 이벤트 수신 테스트")
        fun given_userDeletedEvent_when_consume_then_deleteHistoryOfUser() {
            val producer = createProducer<Void>(broker)

            producer.send(ProducerRecord(KafkaTopic.USER_DELETED, HistoryTestParameters.USER_ID.toString(), null))
            Thread.sleep(1000L)

            Mockito.verify(historyService).deleteByUserId(HistoryTestParameters.USER_ID)
        }
    }

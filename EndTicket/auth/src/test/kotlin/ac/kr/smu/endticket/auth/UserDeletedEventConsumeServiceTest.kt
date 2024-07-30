package ac.kr.smu.endticket.auth

import ac.kr.smu.endticket.auth.service.UserDeletedEventConsumeService
import ac.kr.smu.endticket.common.kafka.constant.KafkaTopic
import ac.kr.smu.endticket.common.kafka.test.createProducer
import org.apache.kafka.clients.producer.ProducerRecord
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.kafka.test.EmbeddedKafkaBroker
import org.springframework.kafka.test.context.EmbeddedKafka

@SpringBootTest(
    classes = [
        KafkaAutoConfiguration::class,
        TokenService::class,
        UserDeletedEventConsumeService::class,
    ],
)
@EmbeddedKafka
class UserDeletedEventConsumeServiceTest
    @Autowired
    constructor(
        @MockBean
        private val tokenService: TokenService,
        private val broker: EmbeddedKafkaBroker,
    ) {
        @Test
        @DisplayName("회원 탈퇴 이벤트를 수신하면 회원의 토큰을 만료시킨다.")
        fun given_userDeletedEvent_when_consume_then_expireTokenOfUser() {
            val producer = createProducer<Unit>(broker)

            producer.send(ProducerRecord(KafkaTopic.USER_DELETED, AuthTestParameters.USER_ID.toString(), null))
            Thread.sleep(1000)

            Mockito.verify(tokenService).expireAccessAndRefreshToken(AuthTestParameters.USER_ID)
        }
    }

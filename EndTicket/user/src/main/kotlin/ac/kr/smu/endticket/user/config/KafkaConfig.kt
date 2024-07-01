package ac.kr.smu.endticket.user.config

import ac.kr.smu.endticket.common.kafka.annotation.EnableAutoKafkaConfig
import ac.kr.smu.endticket.common.web.annotation.EnableAutoAsyncConfig
import org.springframework.context.annotation.Configuration

@Configuration
@EnableAutoKafkaConfig
@EnableAutoAsyncConfig
class KafkaConfig {
}
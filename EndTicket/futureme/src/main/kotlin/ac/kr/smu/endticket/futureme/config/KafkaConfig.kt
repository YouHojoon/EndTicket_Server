package ac.kr.smu.endticket.futureme.config

import ac.kr.smu.endticket.common.kafka.annotation.EnableAutoKafkaConfig
import ac.kr.smu.endticket.common.web.annotation.EnableAutoAsyncConfig
import org.springframework.context.annotation.Configuration

@EnableAutoKafkaConfig
@Configuration
@EnableAutoAsyncConfig
class KafkaConfig

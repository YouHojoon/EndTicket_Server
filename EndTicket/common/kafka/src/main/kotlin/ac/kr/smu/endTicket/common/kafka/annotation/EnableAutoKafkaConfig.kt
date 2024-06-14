package ac.kr.smu.endTicket.common.kafka.annotation

import ac.kr.smu.endTicket.common.kafka.config.AutoKafkaConfig
import org.springframework.context.annotation.Import

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@Import(AutoKafkaConfig::class)
annotation class EnableAutoKafkaConfig

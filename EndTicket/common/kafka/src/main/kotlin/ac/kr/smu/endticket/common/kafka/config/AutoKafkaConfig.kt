package ac.kr.smu.endticket.common.kafka.config

import KafkaMessageService
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean
import org.springframework.kafka.annotation.EnableKafka
import org.springframework.kafka.core.KafkaTemplate


@EnableKafka
class AutoKafkaConfig {
    @Bean
    @ConditionalOnMissingBean(KafkaMessageService::class)
    fun <K:Any, V> kafkaMessageService(kafkaTemplate: KafkaTemplate<K,V>) = KafkaMessageService(kafkaTemplate.also { it.setObservationEnabled(true)})
}
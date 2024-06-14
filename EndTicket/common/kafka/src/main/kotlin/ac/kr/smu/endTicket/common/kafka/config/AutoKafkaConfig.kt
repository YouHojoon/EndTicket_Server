package ac.kr.smu.endTicket.common.kafka.config

import KafkaMessageService
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean
import org.springframework.kafka.annotation.EnableKafka
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.core.ProducerFactory


@EnableKafka
class AutoKafkaConfig {
    @Bean
    @ConditionalOnMissingBean(KafkaTemplate::class)
    fun <K:Any, V> kafkaTemplate(producerFactory: ProducerFactory<K, V>): KafkaTemplate<K, V> {
        val template = KafkaTemplate(producerFactory)
        template.setObservationEnabled(true)
        return template
    }

    @Bean
    @ConditionalOnMissingBean(KafkaMessageService::class)
    fun <K:Any, V> kafkaMessageService(kafkaTemplate: KafkaTemplate<K,V>): KafkaMessageService<K,V> = KafkaMessageService(kafkaTemplate)
}
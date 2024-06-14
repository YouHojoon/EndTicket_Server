package ac.kr.smu.endTicket.common.kafka.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.annotation.EnableKafka
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.core.ProducerFactory

@Configuration
@EnableKafka
class KafkaConfig {
    @Bean
    fun <K:Any, V> kafkaTemplate(producerFactory: ProducerFactory<K, V>): KafkaTemplate<K, V> {
        val template = KafkaTemplate(producerFactory)
        template.setObservationEnabled(true)
        return template
    }
}
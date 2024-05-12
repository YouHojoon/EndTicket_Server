package ac.kr.smu.endTicket.ticket.infra.config

import ac.kr.smu.endTicket.ticket.ui.response.TicketResponse
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.annotation.EnableKafka
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.core.ProducerFactory


@Configuration
@EnableKafka
class KafkaTracingConfig {
    @Bean
    fun kafkaTemplate(producerFactory: ProducerFactory<String, TicketResponse>): KafkaTemplate<String, TicketResponse>{
        val template = KafkaTemplate(producerFactory)
        template.setObservationEnabled(true)
        return template
    }
}
package ac.kr.smu.endticket.common.kafka.config

import KafkaMessageService
import ac.kr.smu.endticket.common.kafka.handler.DLTHandler
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean
import org.springframework.kafka.annotation.EnableKafka
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.retrytopic.DltStrategy
import org.springframework.kafka.retrytopic.RetryTopicConfiguration
import org.springframework.kafka.retrytopic.RetryTopicConfigurationBuilder
import org.springframework.kafka.retrytopic.TopicSuffixingStrategy
import org.springframework.kafka.support.EndpointHandlerMethod

@EnableKafka
class AutoKafkaConfig {
    @Bean
    @ConditionalOnMissingBean(KafkaMessageService::class)
    fun <K : Any, V> kafkaMessageService(kafkaTemplate: KafkaTemplate<K, V>) =
        KafkaMessageService(kafkaTemplate.also { it.setObservationEnabled(true) })

    @Bean
    @ConditionalOnMissingBean(RetryTopicConfiguration::class)
    fun retryableTopic(kafkaTemplate: KafkaTemplate<String, Any>) =
        RetryTopicConfigurationBuilder
            .newInstance()
            .maxAttempts(3)
            .exponentialBackoff(
                5 * 1000,
                2.0,
                20 * 1000,
            ).autoCreateTopics(true, 3, 3)
            .setTopicSuffixingStrategy(TopicSuffixingStrategy.SUFFIX_WITH_INDEX_VALUE)
            .dltProcessingFailureStrategy(DltStrategy.ALWAYS_RETRY_ON_ERROR)
            .dltHandlerMethod(EndpointHandlerMethod(DLTHandler::class.java, "handle"))
            .create(kafkaTemplate)
}

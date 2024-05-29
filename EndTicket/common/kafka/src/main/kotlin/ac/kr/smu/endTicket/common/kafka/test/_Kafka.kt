package ac.kr.smu.endTicket.common.kafka.test

import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.common.serialization.StringDeserializer
import org.springframework.kafka.core.DefaultKafkaConsumerFactory
import org.springframework.kafka.listener.ContainerProperties
import org.springframework.kafka.listener.KafkaMessageListenerContainer
import org.springframework.kafka.listener.MessageListener
import org.springframework.kafka.support.serializer.JsonDeserializer
import org.springframework.kafka.test.EmbeddedKafkaBroker
import org.springframework.kafka.test.utils.ContainerTestUtils
import org.springframework.kafka.test.utils.KafkaTestUtils

inline fun <reified V> createKafkaContainer(broker: EmbeddedKafkaBroker, topic:String): KafkaMessageListenerContainer<String, V> {
    val config = KafkaTestUtils.consumerProps("test", "false", broker)
    val deserializer = JsonDeserializer<V>()
    deserializer.addTrustedPackages(V::class.java.packageName)

    val consumerFactory = DefaultKafkaConsumerFactory(config, StringDeserializer(), deserializer)

    return KafkaMessageListenerContainer(consumerFactory, ContainerProperties(topic))
}

inline fun <reified V> KafkaMessageListenerContainer<String, V>.messageListener(broker: EmbeddedKafkaBroker, crossinline onMessage: (ConsumerRecord<String, V>) -> Unit){
    setupMessageListener(
        MessageListener{
           onMessage(it)
        }
    )

    start()

    ContainerTestUtils.waitForAssignment(this, broker.partitionsPerTopic)
}


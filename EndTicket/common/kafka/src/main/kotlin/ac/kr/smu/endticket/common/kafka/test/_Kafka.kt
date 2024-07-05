package ac.kr.smu.endticket.common.kafka.test

import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.clients.producer.Producer
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.kafka.common.serialization.StringSerializer
import org.jetbrains.annotations.TestOnly
import org.springframework.kafka.core.DefaultKafkaConsumerFactory
import org.springframework.kafka.core.DefaultKafkaProducerFactory
import org.springframework.kafka.listener.ContainerProperties
import org.springframework.kafka.listener.KafkaMessageListenerContainer
import org.springframework.kafka.listener.MessageListener
import org.springframework.kafka.support.serializer.JsonDeserializer
import org.springframework.kafka.support.serializer.JsonSerializer
import org.springframework.kafka.test.EmbeddedKafkaBroker
import org.springframework.kafka.test.utils.ContainerTestUtils
import org.springframework.kafka.test.utils.KafkaTestUtils

/**
 * 테스트를 위한 KafkaMessageListenerContainer를 생성하는 메소드
 * @param V record의 value 타입
 * @param broker 테스트를 위한 카프카 브로커
 * @param topic 테스트할 카프카 토픽
 * @return 생성된 카프카 컨테이너
 */
@TestOnly
inline fun <reified V> createKafkaContainer(
    broker: EmbeddedKafkaBroker,
    topic: String,
): KafkaMessageListenerContainer<String, V> {
    val config = KafkaTestUtils.consumerProps("test", "false", broker)
    val deserializer = JsonDeserializer<V>()
    deserializer.addTrustedPackages(V::class.java.packageName)

    val consumerFactory = DefaultKafkaConsumerFactory(config, StringDeserializer(), deserializer)

    return KafkaMessageListenerContainer(consumerFactory, ContainerProperties(topic))
}

/**
 * KafkaMessageListenerContainer에 MessageListener를 추가하고 컨테이너를 시작하는 메소드
 * @param V record의 value 타입
 * @param broker 테스트를 위한 카프카 브로커
 * @param onMessage 메시지를 수신 시 호출되는 콜백 메소드
 */
@TestOnly
inline fun <reified V> KafkaMessageListenerContainer<String, V>.messageListener(
    broker: EmbeddedKafkaBroker,
    crossinline onMessage: (ConsumerRecord<String, V>) -> Unit,
) {
    setupMessageListener(
        MessageListener {
            onMessage(it)
        },
    )

    start()

    ContainerTestUtils.waitForAssignment(this, broker.partitionsPerTopic)
}

/**
 * 카프카 프로듀서를 생성하는 메소드
 * @param V record의 value 타입
 * @param broker 테스트를 위한 카프카 브로커
 * @return 생성된 카프카 프로듀서
 */
@TestOnly
fun <T> createProducer(broker: EmbeddedKafkaBroker): Producer<String, T> {
    val properties = KafkaTestUtils.producerProps(broker)
    return DefaultKafkaProducerFactory(properties, StringSerializer(), JsonSerializer<T>()).createProducer()
}

package ac.kr.smu.endTicket.common.kafka

import ac.kr.smu.endTicket.common.kafka.constant.KafkaTopic
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.support.SendResult

/**
 * KafkaTemplate을 이용해 메시지를 전송하는 메소드
 * @param topic 카프카 토픽
 * @param message 전송할 메시지
 */
fun <K: Any,V:Any> KafkaTemplate<K, V>.send(topic: KafkaTopic, message: KafkaMessage<K,V>) = send(topic.topicName, message.key, message.payload)
/**
 * KafkaTemplate을 이용해 메시지를 전송하는 메소드
 * @param topic 카프카 토픽
 * @param message 전송할 메시지
 * @param callback 메시지 전송 후 호출될 callback 메소드
 */
inline fun <K: Any,V:Any> KafkaTemplate<K, V>.send(
    topic: KafkaTopic,
    message: KafkaMessage<K,V>,
    noinline callback: (SendResult<K,V>, Throwable?) -> Unit)
= send(topic.topicName, message.key, message.payload).whenCompleteAsync(callback)

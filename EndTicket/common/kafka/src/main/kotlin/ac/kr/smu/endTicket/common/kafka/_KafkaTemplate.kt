package ac.kr.smu.endTicket.common.kafka

import org.springframework.kafka.core.KafkaTemplate

/**
 * KafkaTemplate을 이용해 메시지를 전송하는 메소드
 * @param topic 카프카 토픽
 * @param message 전송할 메시지
 */
fun <K: Any,V:Any> KafkaTemplate<K, V>.send(topic: String, message: KafkaMessage<K,V>) = send(topic, message.key, message.payload)

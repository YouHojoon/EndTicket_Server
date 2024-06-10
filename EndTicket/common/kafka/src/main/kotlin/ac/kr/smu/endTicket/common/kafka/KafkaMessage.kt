package ac.kr.smu.endTicket.common.kafka

/**
 * 카프카 메시지를 추상화한 객체
 * @property key 카프카 메시지의 키
 * @property payload 카프카 메시지의 페이로드
 */
open class KafkaMessage<K:Any, V:Any>(
    val key: K,
    val payload: V
)
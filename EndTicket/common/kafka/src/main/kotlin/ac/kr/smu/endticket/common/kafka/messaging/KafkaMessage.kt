package ac.kr.smu.endticket.common.kafka.messaging

/**
 * 카프카 메시지를 추상화한 객체
 * @property key 카프카 메시지의 키
 * @property payload 카프카 메시지의 페이로드
 */
data class KafkaMessage<out K : Any, out V>(
    val key: K,
    val payload: V? = null,
)

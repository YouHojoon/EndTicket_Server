import ac.kr.smu.endticket.common.kafka.messaging.KafkaMessage
import org.slf4j.LoggerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.support.SendResult
import java.util.concurrent.CompletableFuture

/**
 * 카프카 메시지 전송을 담당하는 서비스
 * @property kafkaTemplate 카프카 메시지 전송을 위한 객체
 * @property K 카프카 메시지 키의 타입
 * @property V 카프카 메시지 값의 타입
 */
class KafkaMessageService<K : Any, V>(
    private val kafkaTemplate: KafkaTemplate<K, V>,
) {
    private val log = LoggerFactory.getLogger(KafkaMessageService::class.java)

    /**
     * 카프카 메시지를 전송하는 메소드
     * @param topic 카프카 토픽
     * @param message 카프카 메시지
     * @return 메시지에 대한 CompletableFuture
     */
    fun send(
        topic: String,
        message: KafkaMessage<K, V>,
    ) = kafkaTemplate.send(topic, message.key, message.payload)

    /**
     * 카프카 메시지들을 전송하는 메소드
     * @param topic 카프카 토픽
     * @param messages 카프카 메시지
     * @return 전송에 성공한 메시지들에 대한 SendResult
     */
    fun send(
        topic: String,
        messages: Collection<KafkaMessage<K, V>>,
    ): Collection<SendResult<K, V>> {
        val futures =
            messages
                .mapIndexed { i, message ->
                    send(topic, message).handle { record, e ->
                        if (e == null) {
                            record
                        } else {
                            log.error("key: ${message.key}, payload: ${message.payload}", e)
                            null
                        }
                    }
                }

        CompletableFuture.allOf(*futures.toTypedArray()).join()

        return futures.mapNotNull { it.join() }
    }
}

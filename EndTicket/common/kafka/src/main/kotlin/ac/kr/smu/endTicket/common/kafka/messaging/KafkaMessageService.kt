import ac.kr.smu.endTicket.common.kafka.messaging.KafkaMessage
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.support.SendResult
import org.springframework.stereotype.Component
import java.util.concurrent.CompletableFuture

/**
 * 상상해보기 완료 이벤트의 메시지 발행을 담당하는 서비스
 * @property kafkaTemplate 카프카 메시지 발행을 위한 클래스
 */
@Component
class KafkaMessageService<K: Any,V>(
    private val kafkaTemplate: KafkaTemplate<K, V>
) {

    /**
     * 상상해보기 이벤트 완료 메시지를 전송하는 메소드
     * @param message 메시지를 발행할 이벤트
     * @return 메시지에 대한 CompletableFuture
     */
    fun send(topic: String, message: KafkaMessage<K,V>) = kafkaTemplate.send(topic,message.key, message.payload)


    /**
     * 상상해보기 이벤트 완료 메시지들을 전송하는 메소드
     * @param messages 메시지를 전송할 이벤트들
     * @return 메시지들에 대한 CompletableFuture
     */
    fun send(topic: String, messages: Collection<KafkaMessage<K,V>>): Collection<CompletableFuture<SendResult<K, V>>> = messages.map { send(topic, it) }
}
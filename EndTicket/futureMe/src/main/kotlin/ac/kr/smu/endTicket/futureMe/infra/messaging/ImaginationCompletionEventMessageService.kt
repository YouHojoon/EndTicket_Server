package ac.kr.smu.endTicket.futureMe.infra.messaging

import ac.kr.smu.endTicket.common.kafka.constant.KafkaTopic
import ac.kr.smu.endTicket.common.kafka.send
import ac.kr.smu.endTicket.futureMe.domain.event.model.ImaginationCompletionEvent
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.support.SendResult
import org.springframework.stereotype.Component
import java.util.concurrent.CompletableFuture

/**
 * 상상해보기 완료 이벤트의 메시지 발행을 담당하는 서비스
 * @property kafkaTemplate 카프카 메시지 발행을 위한 클래스
 */
@Component
class ImaginationCompletionEventMessageService(
    private val kafkaTemplate: KafkaTemplate<String, ImaginationCompletionEventResponse>
) {

    /**
     * 상상해보기 이벤트 완료 메시지를 전송하는 메소드
     * @param message 메시지를 발행할 이벤트
     * @param callback 메시지 발행 후 실행할 함수
     */
    fun sendMessage(message: ImaginationCompletionEventMessage, callback: (SendResult<String, ImaginationCompletionEventResponse>?, Throwable?) -> Unit){
        kafkaTemplate.send(KafkaTopic.IMAGINATION_COMPLETION, message, callback)
    }


    /**
     * 상상해보기 이벤트 완료 메시지들을 전송하는 메소드
     * @param messages 메시지를 전송할 이벤트들
     * @param callback 메시지 발행 후 실행할 함수
     */
    fun sendMessages(messages: Collection<ImaginationCompletionEventMessage>,
                     callback: (Collection<Pair<SendResult<String, ImaginationCompletionEventResponse>?, Throwable?>>) -> Unit){
        val futures = messages.map { kafkaTemplate.send(KafkaTopic.IMAGINATION_COMPLETION, it, callback) }.toTypedArray()

        CompletableFuture
            .allOf(*futures)
            .thenRun {
                val results = futures.map {
                    try {
                        it.get() to null
                    }
                    catch (e: Exception){
                        null to e
                    }
                }
                callback(results)
            }.join()
    }


}
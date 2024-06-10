package ac.kr.smu.endTicket.futureMe.service

import ac.kr.smu.endTicket.common.kafka.constant.KafkaTopic
import ac.kr.smu.endTicket.futureMe.domain.event.model.ImaginationCompletionEvent
import ac.kr.smu.endTicket.futureMe.ui.response.ImaginationCompletionEventResponse
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.support.SendResult
import org.springframework.stereotype.Component
import java.util.concurrent.CompletableFuture

/**
 * 상상해보기 완료 이벤트의 메시지 발행을 담당하는 서비스
 * @property repo 이벤트의 메시지 발행 여부의 변경을 저장하기 위한 저장소
 * @property kafkaTemplate 카프카 메시지 발행을 위한 클래스
 */
@Component
class ImaginationCompletionEventMessageService(
    private val kafkaTemplate: KafkaTemplate<String, ImaginationCompletionEventResponse>
) {

    /**
     * 상상해보기 이벤트 완료 메시지를 전송하는 메소드, 전송에 성공하면 이를 반영하고 실패하면 로그를 남긴다.
     * @param event 메시지를 발행할 이벤트
     * @param callback 메시지 발행 후 실행할 함수
     */
    fun sendMessage(event: ImaginationCompletionEvent, callback: (SendResult<String, ImaginationCompletionEventResponse>, Throwable?) -> Unit) = kafkaTemplate.send(event, callback)


    /**
     * 상상해보기 이벤트 완료 메시지들을 전송하는 메소드, 전송에 성공한 것들을 기록한다.
     * @param events 메시지를 전송할 이벤트들
     * @param callback 메시지 발행 후 실행할 함수
     */
    fun sendMessages(events: Collection<ImaginationCompletionEvent>,
                     callback: (SendResult<String, ImaginationCompletionEventResponse>, Throwable?) -> Unit) = events.forEach { kafkaTemplate.send(it, callback) }

    /**
     * KafkaTemplate를 통해 메시지를 전송하는 함수
     * @param event 메시지를 전송할 이벤트
     * @param callback 메시지 발행 후 실행할 함수
     */
    private inline fun KafkaTemplate<String, ImaginationCompletionEventResponse>.send(
        event: ImaginationCompletionEvent,
        noinline callback: (SendResult<String, ImaginationCompletionEventResponse>, Throwable?) -> Unit
    ): CompletableFuture<SendResult<String, ImaginationCompletionEventResponse>> {
        val message = event.toMessage()

        return send(KafkaTopic.IMAGINATION_COMPLETION, message.key.toString(), message.payload)
            .whenCompleteAsync(callback)
    }
}
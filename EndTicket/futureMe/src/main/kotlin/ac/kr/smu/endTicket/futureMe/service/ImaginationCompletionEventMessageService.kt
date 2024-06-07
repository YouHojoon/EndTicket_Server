package ac.kr.smu.endTicket.futureMe.service

import ac.kr.smu.endTicket.common.kafka.constant.KafkaTopic
import ac.kr.smu.endTicket.futureMe.domain.event.repository.EventRepository
import ac.kr.smu.endTicket.futureMe.domain.event.model.ImaginationCompletionEvent
import ac.kr.smu.endTicket.futureMe.ui.response.ImaginationCompletionEventResponse
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.support.SendResult
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import java.util.concurrent.CompletableFuture

/**
 * 상상해보기 완료 이벤트의 메시지 발행을 담당하는 서비스
 * @property repo 이벤트의 메시지 발행 여부의 변경을 저장하기 위한 저장소
 * @property kafkaTemplate 카프카 메시지 발행을 위한 클래스
 */
@Service
class ImaginationCompletionEventMessageService(
    private val repo: EventRepository,
    private val kafkaTemplate: KafkaTemplate<String, ImaginationCompletionEventResponse>
) {
    private val log = LoggerFactory.getLogger(ImaginationCompletionEventMessageService::class.java)

    /**
     * 상상해보기 이벤트 완료 메시지를 전송하는 메소드, 전송에 성공하면 이를 반영하고 실패하면 로그를 남긴다.
     * @param event 메시지를 발행할 이벤트
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun sendMessage(event: ImaginationCompletionEvent){
        kafkaTemplate
            .send(event)
            .whenCompleteAsync { _, e ->
                if (e == null){
                    event.successSend()
                    repo.save(event)
                }
                else
                    log.error(event, e)
            }
    }

    /**
     * 상상해보기 이벤트 완료 메시지들을 전송하는 메소드, 전송에 성공한 것들을 기록한다.
     * @param events 전송할 이벤트들
     */
    fun sendMessages(events: Collection<ImaginationCompletionEvent>) {
        events
            .forEach {
                kafkaTemplate.send(it)
                    .whenCompleteAsync { _, e ->
                        if (e == null) {
                            it.successSend()
                        } else
                            log.error(it, e)

                    }
            }
    }

    private fun Logger.error(event: ImaginationCompletionEvent, e:Throwable){
        val message = event.toMessage()
        error("{key: ${message.key}, payload:${message.payload}}", e)
    }
    private fun KafkaTemplate<String, ImaginationCompletionEventResponse>.send(event: ImaginationCompletionEvent): CompletableFuture<SendResult<String, ImaginationCompletionEventResponse>> {
        val message = event.toMessage()

        return send(KafkaTopic.IMAGINATION_COMPLETION, message.key.toString(), message.payload)
    }
}
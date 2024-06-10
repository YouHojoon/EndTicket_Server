package ac.kr.smu.endTicket.ticket.infra.messaging

import ac.kr.smu.endTicket.common.kafka.constant.KafkaTopic
import ac.kr.smu.endTicket.ticket.domain.model.TicketCompletionEvent
import ac.kr.smu.endTicket.ticket.domain.repository.TicketCompletionEventRepository

import ac.kr.smu.endTicket.ticket.ui.response.TicketResponse
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.support.SendResult
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import java.util.concurrent.CompletableFuture

/**
 * 티켓 완료 이벤트의 메시지를 전송하는 서비스
 * @property kafkaTemplate 메시지 전송을 위한 객체
 * @property repo 이벤트의 발행 후 발행 완료를 저장하기 위한 저장
 */
@Service
class TicketCompletionEventMessageService(
    private val kafkaTemplate: KafkaTemplate<String, TicketResponse>,
    private val repo: TicketCompletionEventRepository

) {
    private val log = LoggerFactory.getLogger(TicketCompletionEventMessageService::class.java)
    /**
     * 이벤트 전송, 전송이 완료되면 완료 여부를 저장한다.
     * @param event 전송할 이벤트
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun sendMessage(event: TicketCompletionEvent){
        kafkaTemplate.send(event)
            .whenCompleteAsync { _, e ->
                if (e == null){
                    event.successSend()
                    repo.save(event)
                }
                else
                    log.error(event,e)
            }
    }

    /**
     * 이벤트들 전송, 전송이 완료되면 완료 여부를 기록한다.
     * @param events 전송할 이벤트들
     */
    fun sendMessages(events: Collection<TicketCompletionEvent>){
        events.forEach {
            kafkaTemplate.send(it)
                .whenCompleteAsync { _, e ->
                    if (e == null)
                        it.successSend()
                    else
                        log.error(it,e)
                }
        }
    }

    private fun Logger.error(event: TicketCompletionEvent, e: Throwable){
        val message = event.toMessage()
        error("{key: ${message.key}, payload: ${message.payload}}", e)
    }
    private fun KafkaTemplate<String, TicketResponse>.send(event: TicketCompletionEvent): CompletableFuture<SendResult<String,TicketResponse>>{
        val message = event.toMessage()
        return send(KafkaTopic.TICKET_COMPLETION,"${message.key}", message.payload)
    }
}
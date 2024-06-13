package ac.kr.smu.endTicket.ticket.infra.messaging

import ac.kr.smu.endTicket.common.kafka.constant.KafkaTopic
import ac.kr.smu.endTicket.common.kafka.send
import ac.kr.smu.endTicket.ticket.domain.model.TicketCompletionEvent
import ac.kr.smu.endTicket.ticket.domain.repository.TicketCompletionEventRepository

import ac.kr.smu.endTicket.ticket.ui.response.TicketResponse
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.support.SendResult
import org.springframework.stereotype.Component
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import java.util.concurrent.CompletableFuture

/**
 * 티켓 완료 이벤트의 메시지를 전송하는 서비스
 * @property kafkaTemplate 메시지 전송을 위한 객체
 */
@Component
class TicketCompletionEventMessageService(
    private val kafkaTemplate: KafkaTemplate<String, TicketResponse>,

) {
    /**
     * 이벤트 전송
     * @param message 전송할 메시지
     * @param callback 전송 후 호출할 콜백 메소드
     */
    fun sendMessage(message: TicketCompletionEventMessage, callback: (SendResult<String, TicketResponse>, Throwable?) -> Unit){
        kafkaTemplate.send(KafkaTopic.TICKET_COMPLETION, message).whenCompleteAsync(callback)
    }


    /**
     * 이벤트들 전송
     * @param events 전송할 이벤트들
     * @param callback 전송 후 호출할 콜백 메소드
     */
    fun sendMessages(messages: Collection<TicketCompletionEventMessage>, callback: (SendResult<String, TicketResponse>, Throwable?) -> Unit){
        messages.forEach {kafkaTemplate.send(KafkaTopic.TICKET_COMPLETION, it).whenCompleteAsync(callback)}
    }
}
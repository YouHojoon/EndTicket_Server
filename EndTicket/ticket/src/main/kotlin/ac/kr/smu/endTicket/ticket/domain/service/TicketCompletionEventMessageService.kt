package ac.kr.smu.endTicket.ticket.domain.service

import ac.kr.smu.endTicket.common.kafka.constant.KafkaTopic
import ac.kr.smu.endTicket.ticket.domain.model.TicketCompletionEvent
import ac.kr.smu.endTicket.ticket.domain.repository.TicketCompletionEventRepository

import ac.kr.smu.endTicket.ticket.ui.response.TicketResponse
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional

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
    /**
     * 이벤트 전송, 전송이 완료되면 완료 여부를 저장한다.
     * @param event 전송할 이벤트
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun send(event: TicketCompletionEvent){
        val response = event.toResponse()

        kafkaTemplate
            .send(KafkaTopic.TICKET_COMPLETION,"${response.key}", response.payload)
            .whenCompleteAsync { _, e ->
                if (e == null){
                    event.successPublish()
                    repo.save(event)
                }
            }
    }
}
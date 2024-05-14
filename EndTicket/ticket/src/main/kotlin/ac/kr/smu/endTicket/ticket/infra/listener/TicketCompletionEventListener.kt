package ac.kr.smu.endTicket.ticket.infra.listener

import ac.kr.smu.endTicket.constant.KafkaTopic
import ac.kr.smu.endTicket.ticket.domain.model.TicketCompletionEvent
import ac.kr.smu.endTicket.ticket.domain.repository.TicketCompletionEventRepository
import ac.kr.smu.endTicket.ticket.domain.service.TicketCompletionEventMessageService
import ac.kr.smu.endTicket.ticket.ui.response.TicketResponse
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import org.springframework.transaction.event.TransactionPhase
import org.springframework.transaction.event.TransactionalEventListener

/**
 * 티켓 완료 이벤트를 위한 Listener
 * @property repo 이벤트를 저장하기 위한 저장소
 * @property messageService 메시지를 전송하기 위한 서비스
 */
@Component
class TicketCompletionEventListener(
    private val repo: TicketCompletionEventRepository,
    private val messageService: TicketCompletionEventMessageService
) {
    @Transactional
    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    fun saveEvent(event: TicketCompletionEvent){
        repo.save(event)
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun sendMessage(event: TicketCompletionEvent){
        messageService.send(event)
    }
}
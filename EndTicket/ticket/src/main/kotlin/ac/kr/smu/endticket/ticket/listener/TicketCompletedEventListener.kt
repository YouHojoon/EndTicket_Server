package ac.kr.smu.endticket.ticket.listener

import KafkaMessageService
import ac.kr.smu.endticket.common.kafka.constant.KafkaTopic
import ac.kr.smu.endticket.ticket.domain.model.TicketCompletedEvent
import ac.kr.smu.endticket.ticket.domain.repository.TicketCompletedEventRepository
import ac.kr.smu.endticket.ticket.infra.messaging.TicketCompletedEventResponse
import org.slf4j.LoggerFactory
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
class TicketCompletedEventListener(
    private val repo: TicketCompletedEventRepository,
    private val messageService: KafkaMessageService<String, TicketCompletedEventResponse>
) {
    private val log = LoggerFactory.getLogger(TicketCompletedEventListener::class.java)
    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    fun saveEvent(event: TicketCompletedEvent){
        repo.save(event)
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun sendMessage(event: TicketCompletedEvent){
        val message = event.toMessage()

        messageService.send(KafkaTopic.TICKET_COMPLETED,message).whenCompleteAsync { _, e ->
            if (e == null)
                repo.save(event.also { it.successSend() })
            else {
                log.error("{key: ${message.key}}, payload: ${message.payload}", e)
            }
        }
    }
}
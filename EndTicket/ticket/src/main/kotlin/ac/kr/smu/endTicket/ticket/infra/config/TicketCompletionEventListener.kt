package ac.kr.smu.endTicket.ticket.infra.config

import ac.kr.smu.endTicket.constant.KafkaTopic
import ac.kr.smu.endTicket.ticket.domain.event.TicketCompletionEvent
import ac.kr.smu.endTicket.ticket.ui.response.TicketResponse
import org.springframework.context.event.EventListener
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component
import org.springframework.transaction.event.TransactionPhase
import org.springframework.transaction.event.TransactionalEventListener
import kotlin.math.log

@Component
class TicketCompletionEventListener(
    private val kafkaTemplate: KafkaTemplate<String, TicketResponse>
) {
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun sendMessage(event: TicketCompletionEvent){
        kafkaTemplate
            .send(KafkaTopic.TICKET_COMPLETION,"${event.userID}", event.response)
    }
}
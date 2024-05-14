package ac.kr.smu.endTicket.ticket.domain.service

import ac.kr.smu.endTicket.constant.KafkaTopic
import ac.kr.smu.endTicket.ticket.domain.model.TicketCompletionEvent
import ac.kr.smu.endTicket.ticket.domain.repository.TicketCompletionEventRepository
import ac.kr.smu.endTicket.ticket.ui.response.TicketCompletionEventResponse
import ac.kr.smu.endTicket.ticket.ui.response.TicketResponse
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional

@Service
class TicketCompletionEventMessageService(
    private val kafkaTemplate: KafkaTemplate<String, TicketResponse>,
    private val repo: TicketCompletionEventRepository

) {
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
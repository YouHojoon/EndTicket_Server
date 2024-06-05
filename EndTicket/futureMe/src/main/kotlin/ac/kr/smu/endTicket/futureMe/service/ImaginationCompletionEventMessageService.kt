package ac.kr.smu.endTicket.futureMe.service

import ac.kr.smu.endTicket.common.kafka.constant.KafkaTopic
import ac.kr.smu.endTicket.futureMe.domain.event.EventRepository
import ac.kr.smu.endTicket.futureMe.domain.event.ImaginationCompletionEvent
import ac.kr.smu.endTicket.futureMe.ui.response.ImaginationCompletionEventMessage
import ac.kr.smu.endTicket.futureMe.ui.response.ImaginationCompletionEventResponse
import org.slf4j.LoggerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional

@Service
class ImaginationCompletionEventMessageService(
    private val repo: EventRepository,
    private val kafkaTemplate: KafkaTemplate<String, ImaginationCompletionEventResponse>
) {
    private val log = LoggerFactory.getLogger(ImaginationCompletionEventMessageService::class.java)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun sendMessage(event: ImaginationCompletionEvent){
        val message = event.toMessage()

        kafkaTemplate
            .send(KafkaTopic.IMAGINATION_COMPLETION, message.key.toString(), message.payload)
            .whenCompleteAsync { _, e ->
                if (e == null){
                    event.successSend()
                    repo.save(event)
                }
                else
                    log.error("{key: ${message.key}, payload:${message.payload}}", e)

            }
    }
}
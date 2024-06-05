package ac.kr.smu.endTicket.futureMe.service

import ac.kr.smu.endTicket.common.kafka.constant.KafkaTopic
import ac.kr.smu.endTicket.futureMe.domain.event.EventRepository
import ac.kr.smu.endTicket.futureMe.domain.event.TicketCompletionEvent
import ac.kr.smu.endTicket.futureMe.ui.response.TicketResponse
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.support.Acknowledgment
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Duration

@Service
class TicketCompletionEventConsumeService(
    private val repo: EventRepository,
    private val futureMeService: FutureMeService
) {
    private val log = LoggerFactory.getLogger(TicketCompletionEventConsumeService::class.java)
    @KafkaListener(topics = [KafkaTopic.TICKET_COMPLETION])
    @Transactional
    fun consume(record: ConsumerRecord<String, TicketResponse>, ack: Acknowledgment){
        try {
            if (repo.findByEventIDAndType(record.value().id, "TicketCompletionEvent") == null){
                val event = TicketCompletionEvent(record.value().id, record.key().toLong())

                futureMeService.gainExperiencePoints(event)
                repo.save(event)
            }

            ack.acknowledge()
        }catch (e: Exception){
            log.error("{ticketID: ${record.value().id}, userID: ${record.key()}}", e)
            ack.nack(
                Duration.ofSeconds(5)
            )
        }



    }
}
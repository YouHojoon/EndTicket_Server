package ac.kr.smu.endticket.history.service

import ac.kr.smu.endticket.common.kafka.constant.KafkaTopic
import ac.kr.smu.endticket.history.domain.model.TicketHistory
import ac.kr.smu.endticket.history.domain.repository.TicketHistoryRepository
import ac.kr.smu.endticket.history.infra.messaging.TicketCompletedEventResponse
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.support.Acknowledgment
import org.springframework.stereotype.Service
import java.time.Duration

@Service
class TicketCompletedEventConsumeService(
    private val repo: TicketHistoryRepository
) {
    private val log = LoggerFactory.getLogger(TicketCompletedEventConsumeService::class.java)

    /**
     * 티켓 완료 이벤트를 수신하는 메소드, 수신한 이벤트를 기록으로 저장한다.
     * @param record 수신한 이벤트
     * @param ack kafka commit을 위한 객체
     */
    @KafkaListener(topics = [KafkaTopic.TICKET_COMPLETION])
    fun consume(record: ConsumerRecord<String,TicketCompletedEventResponse>, ack: Acknowledgment){
        val response = record.value()
        try {
            if (!repo.existsById(response.id))
                 repo.save(TicketHistory.from(response, record.key().toLong()))

            ack.acknowledge()
        }catch (e: Exception){
            log.error("{ticketId: ${response.id}, userId: ${record.key()}}", e)
            ack.nack(Duration.ofSeconds(5))
        }

    }
}
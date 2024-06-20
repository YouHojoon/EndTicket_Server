package ac.kr.smu.endticket.history.service

import ac.kr.smu.endticket.common.kafka.constant.KafkaTopic
import ac.kr.smu.endticket.history.domain.model.ImaginationHistory
import ac.kr.smu.endticket.history.domain.model.TicketHistory
import ac.kr.smu.endticket.history.domain.repository.TicketHistoryRepository
import ac.kr.smu.endticket.history.infra.messaging.EventResponse
import ac.kr.smu.endticket.history.infra.messaging.ImaginationCompletedEventResponse
import ac.kr.smu.endticket.history.infra.messaging.TicketCompletedEventResponse
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.support.Acknowledgment
import org.springframework.stereotype.Service
import java.time.Duration

@Service
class EventConsumeService(
    private val repo: TicketHistoryRepository
) {
    private val log = LoggerFactory.getLogger(EventConsumeService::class.java)

    /**
     * 이벤트를 수신하는 메소드, 수신한 이벤트를 기록으로 저장한다.
     * @param record 수신한 이벤트
     * @param ack kafka commit을 위한 객체
     */
    @KafkaListener(topics = [KafkaTopic.TICKET_COMPLETION, KafkaTopic.IMAGINATION_COMPLETION])
    fun consume(record: ConsumerRecord<String,EventResponse>, ack: Acknowledgment){
        val response = record.value()
        val userId = record.key().toLong()
        val (type, entity) = when(record.topic()){
            KafkaTopic.TICKET_COMPLETION -> TicketHistory::class to TicketHistory.from(response as TicketCompletedEventResponse,userId)
            KafkaTopic.IMAGINATION_COMPLETION -> ImaginationHistory::class to ImaginationHistory.from(response as ImaginationCompletedEventResponse,userId)
            else -> throw IllegalStateException("${record.topic()}은 알 수 없는 토픽입니다.")
        }

        try {
            if (!repo.existsBySpecificIdAndType(response.id, type))
                repo.save(entity)

            ack.acknowledge()
        }catch (e: Exception){
            log.error("{id: ${response.id}, userId: ${record.key()}, topic: ${record.topic()}", e)
            ack.nack(Duration.ofSeconds(5))
        }
    }
}
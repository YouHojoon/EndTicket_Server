package ac.kr.smu.endticket.history.service

import ac.kr.smu.endticket.common.kafka.constant.KafkaTopic
import ac.kr.smu.endticket.history.domain.converter.HistoryTypeConverter
import ac.kr.smu.endticket.history.domain.model.History
import ac.kr.smu.endticket.history.domain.model.ImaginationHistory
import ac.kr.smu.endticket.history.domain.model.TicketHistory
import ac.kr.smu.endticket.history.domain.repository.HistoryRepository
import ac.kr.smu.endticket.history.infra.messaging.EventResponse
import ac.kr.smu.endticket.history.infra.messaging.ImaginationCompletedEventResponse
import ac.kr.smu.endticket.history.infra.messaging.TicketCompletedEventResponse
import ac.kr.smu.endticket.history.ui.response.HistoryCount
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.slf4j.LoggerFactory
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.support.Acknowledgment
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Duration

@Service
class EventConsumeService(
    private val repo: HistoryRepository,
    private val redisTemplate: RedisTemplate<String, Any>
) {
    private val log = LoggerFactory.getLogger(EventConsumeService::class.java)

    /**
     * 이벤트를 수신하는 메소드, 수신한 이벤트를 기록으로 저장한다.
     * @param record 수신한 이벤트
     * @param ack kafka commit을 위한 객체
     * @throws IllegalArgumentException 이벤트 레코드의 토픽이 지원하지 않는 토픽일 때
     */
    @Transactional
    @KafkaListener(topics = [KafkaTopic.TICKET_COMPLETED, KafkaTopic.IMAGINATION_COMPLETED])
    fun consume(record: ConsumerRecord<String, out EventResponse>, ack: Acknowledgment){
        val response = record.value()
        val userId = record.key().toLong()
        val (type, entity) = when(record.topic()){
            KafkaTopic.TICKET_COMPLETED -> History.Type.TICKET to TicketHistory.from(response as TicketCompletedEventResponse,userId)
            KafkaTopic.IMAGINATION_COMPLETED -> History.Type.IMAGINATION to ImaginationHistory.from(response as ImaginationCompletedEventResponse,userId)
            else -> throw IllegalArgumentException("${record.topic()}은 지원하지 않는 토픽입니다.")
        }

        try {
            if (!repo.existsBySpecificIdAndType(response.id, type))
                redisTemplate.updateCountIfPresent(userId, repo.save(entity))


            ack.acknowledge()
        }catch (e: Exception){
            log.error("{id: ${response.id}, userId: ${record.key()}, topic: ${record.topic()}", e)
            ack.nack(Duration.ofSeconds(5))
        }
    }

    /**
     * 기록 개수가 캐시에 있으면 업데이트 하는 메소드
     * @param userId 사용자 id
     * @param type 새로 저장된 기록 종류
     * @throws IllegalArgumentException 기록의 타입이 지원하지 않는 타입일 때
     */
    private fun RedisTemplate<String,Any>.updateCountIfPresent(userId:Long, history: History){
        val ops = opsForValue()
        val key = "history-count::$userId"
        val count = ops.get(key) as? HistoryCount ?: return

        val newCount = when(history){
            is TicketHistory-> HistoryCount(count.ticketHistoryCount + 1, count.ticketSwipeCount + history.swipeCount, count.imaginationHistoryCount)
            is ImaginationHistory -> HistoryCount(count.ticketHistoryCount,count.ticketSwipeCount,count.imaginationHistoryCount + 1)
            else -> throw IllegalArgumentException("$${history::class}는 지원하지 않는 타입입니다.")
        }

        ops.set(key,newCount)
    }
}
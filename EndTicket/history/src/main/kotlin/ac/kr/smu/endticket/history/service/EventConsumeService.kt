package ac.kr.smu.endticket.history.service

import ac.kr.smu.endticket.common.kafka.constant.KafkaTopic
import ac.kr.smu.endticket.history.infra.messaging.EventResponse
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.support.Acknowledgment
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class EventConsumeService(
    private val historyService: HistoryService,
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
    fun consume(
        record: ConsumerRecord<String, out EventResponse>,
        ack: Acknowledgment,
    ) {
        val response = record.value()
        val userId = record.key().toLong()

        historyService.saveHistory(response, userId)

        ack.acknowledge()
    }
}

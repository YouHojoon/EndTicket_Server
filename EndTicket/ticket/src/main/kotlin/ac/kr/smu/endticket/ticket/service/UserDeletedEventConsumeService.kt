package ac.kr.smu.endticket.ticket.service

import ac.kr.smu.endticket.common.kafka.constant.KafkaTopic
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.support.Acknowledgment
import org.springframework.stereotype.Service

@Service
class UserDeletedEventConsumeService(
    private val service: TicketService,
) {
    private val log = LoggerFactory.getLogger(UserDeletedEventConsumeService::class.java)

    @KafkaListener(topics = [KafkaTopic.USER_DELETED])
    fun consume(
        record: ConsumerRecord<String, Unit>,
        ack: Acknowledgment,
    ) {
        val userId = record.key().toLong()
        service.deleteByUserId(userId)

        ack.acknowledge()
    }
}

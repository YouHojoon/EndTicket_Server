package ac.kr.smu.endTicket.auth.service

import ac.kr.smu.endticket.common.kafka.constant.KafkaTopic
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.support.Acknowledgment
import org.springframework.stereotype.Service

@Service
class UserDeletedEventConsumeService(
    private val tokenService: TokenService,
) {
    @KafkaListener(topics = [KafkaTopic.USER_DELETED])
    fun consume(
        record: ConsumerRecord<String, Unit>,
        ack: Acknowledgment,
    ) {
        val userId = record.key().toLong()

        tokenService.expireAccessAndRefreshToken(userId)
        ack.acknowledge()
    }
}

package ac.kr.smu.endticket.auth.service

import ac.kr.smu.endticket.auth.infra.oauth2.RedisRefreshTokenService
import ac.kr.smu.endticket.common.kafka.constant.KafkaTopic
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.support.Acknowledgment
import org.springframework.stereotype.Service

@Service
class UserDeletedEventConsumeService(
    private val refreshTokenService: RedisRefreshTokenService,
) {
    @KafkaListener(topics = [KafkaTopic.USER_DELETED])
    fun consume(
        record: ConsumerRecord<String, Unit>,
        ack: Acknowledgment,
    ) {
        refreshTokenService.removeByUserId(record.key())
        ack.acknowledge()
    }
}

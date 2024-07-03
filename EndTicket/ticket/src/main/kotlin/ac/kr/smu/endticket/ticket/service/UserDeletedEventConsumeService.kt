package ac.kr.smu.endticket.ticket.service

import ac.kr.smu.endticket.common.kafka.constant.KafkaTopic
import ac.kr.smu.endticket.ticket.domain.repository.TicketRepository
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.support.Acknowledgment
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Duration

@Service
class UserDeletedEventConsumeService(
    private val repo: TicketRepository
) {
    private val log = LoggerFactory.getLogger(UserDeletedEventConsumeService::class.java)

    @KafkaListener(topics = [KafkaTopic.USER_DELETED])
    @Transactional
    fun consume(record: ConsumerRecord<String, Void>, ack: Acknowledgment){
        try{
            val userId = record.key().toLong()
            repo.deleteByUserId(userId)

            ack.acknowledge()
        }catch (e: Exception){
            log.error("회원 탈퇴 이벤트 수신 실패", e)
            ack.nack(Duration.ofSeconds(5))
        }
    }
}
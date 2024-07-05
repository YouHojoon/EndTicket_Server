package ac.kr.smu.endticket.futureme.service

import ac.kr.smu.endticket.common.kafka.constant.KafkaTopic
import ac.kr.smu.endticket.futureme.domain.event.repository.EventRepository
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.support.Acknowledgment
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Duration

@Service
class UserDeletedEventConsumeService(
    private val futureMeService: FutureMeService,
    private val imaginationService: ImaginationService,
    private val eventRepository: EventRepository,
) {
    private val log = LoggerFactory.getLogger(UserDeletedEventConsumeService::class.java)

    @KafkaListener(topics = [KafkaTopic.USER_DELETED])
    @Transactional
    fun consume(
        record: ConsumerRecord<String, Void>,
        ack: Acknowledgment,
    ) {
        val userId = record.key().toLong()

        eventRepository.deleteByUserId(userId)
        futureMeService.deleteFutureMe(userId)
        imaginationService.deleteByUserId(userId)

        ack.acknowledge()
    }
}

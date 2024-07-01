package ac.kr.smu.endticket.user.job

import KafkaMessageService
import ac.kr.smu.endticket.common.kafka.constant.KafkaTopic
import ac.kr.smu.endticket.common.kafka.messaging.KafkaMessage
import ac.kr.smu.endticket.user.domain.repository.UserDeletedEventRepository
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.LocalDateTime

@Component
class UserDeletedEventJob(
    private val repo: UserDeletedEventRepository,
    private val messageService: KafkaMessageService<String, Void>
) {

    @Scheduled(fixedDelayString = "\${schedules.resend-user-deleted-event.fixed-delay}", initialDelayString = "\${schedules.resend-user-deleted-event.inital-delay}")
    fun resend(){
        val events = repo
            .findByAuditCreatedAtBefore(LocalDateTime.now().minusMinutes(10))
        val messages = events.map { KafkaMessage<String, Void>(it.id.toString(), null) }
        messageService.send(KafkaTopic.USER_DELETED, )
    }
}
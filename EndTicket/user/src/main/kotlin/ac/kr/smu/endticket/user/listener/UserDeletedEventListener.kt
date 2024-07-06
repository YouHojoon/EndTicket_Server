package ac.kr.smu.endticket.user.listener

import KafkaMessageService
import ac.kr.smu.endticket.common.kafka.constant.KafkaTopic
import ac.kr.smu.endticket.user.domain.model.UserDeletedEvent
import ac.kr.smu.endticket.user.domain.repository.UserDeletedEventRepository
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import org.springframework.transaction.event.TransactionPhase
import org.springframework.transaction.event.TransactionalEventListener

@Component
class UserDeletedEventListener(
    private val repo: UserDeletedEventRepository,
    private val messageService: KafkaMessageService<String, Unit>,
) {
    private val log = LoggerFactory.getLogger(UserDeletedEventListener::class.java)

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    fun saveEvent(event: UserDeletedEvent) {
        repo.save(event)
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun sendMessage(event: UserDeletedEvent) {
        val message = event.toMessage()

        messageService
            .send(KafkaTopic.USER_DELETED, message)
            .whenComplete { _, e ->
                if (e == null) {
                    repo.delete(event)
                } else {
                    log.error("회원 탈퇴 메시지 전송 실패 : {key : ${message.key}}", e)
                }
            }
    }
}

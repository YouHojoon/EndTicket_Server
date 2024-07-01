package ac.kr.smu.endticket.user.listener

import KafkaMessageService
import ac.kr.smu.endticket.common.kafka.messaging.KafkaMessage
import ac.kr.smu.endticket.user.domain.model.UserDeletedEvent
import ac.kr.smu.endticket.user.domain.repository.UserDeletedEventRepository
import ac.kr.smu.endticket.user.infra.messaging.UserDeletedEventResponse
import org.springframework.context.event.EventListener
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import org.springframework.transaction.event.TransactionPhase
import org.springframework.transaction.event.TransactionalEventListener

@Component
class UserDeletedEventListener(
    private val repo: UserDeletedEventRepository,
    private val messageService: KafkaMessageService<String, Void>
) {
    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    fun saveEvent(event: UserDeletedEvent){
        repo.save(event)
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun sendMessage(event: UserDeletedEvent){
        

    }
}
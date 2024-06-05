package ac.kr.smu.endTicket.futureMe.listener

import ac.kr.smu.endTicket.futureMe.domain.event.EventRepository
import ac.kr.smu.endTicket.futureMe.domain.event.ImaginationCompletionEvent
import ac.kr.smu.endTicket.futureMe.service.ImaginationCompletionEventMessageService
import org.springframework.context.event.EventListener
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import org.springframework.transaction.event.TransactionPhase
import org.springframework.transaction.event.TransactionalEventListener

@Component
class ImaginationCompletionEventListener(
    val repo: EventRepository,
    val messageService: ImaginationCompletionEventMessageService
) {
    @Transactional
    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    fun saveEvent(event: ImaginationCompletionEvent){
        repo.save(event)
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun sendEvent(event: ImaginationCompletionEvent){
        messageService.sendMessage(event)
    }
}
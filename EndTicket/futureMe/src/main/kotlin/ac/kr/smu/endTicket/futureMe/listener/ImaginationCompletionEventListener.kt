package ac.kr.smu.endTicket.futureMe.listener

import ac.kr.smu.endTicket.futureMe.domain.event.repository.EventRepository
import ac.kr.smu.endTicket.futureMe.domain.event.model.ImaginationCompletionEvent
import ac.kr.smu.endTicket.futureMe.service.ImaginationCompletionEventMessageService
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import org.springframework.transaction.event.TransactionPhase
import org.springframework.transaction.event.TransactionalEventListener

/**
 * 상상해보기 완료 이벤트를 처리하는 클래스
 * @property repo 이벤트를 저장하기 위한 저장소
 * @property messageService 메시지를 전송하기 위한 서비스
 */
@Component
class ImaginationCompletionEventListener(
    val repo: EventRepository,
    val messageService: ImaginationCompletionEventMessageService
) {
    /**
     * 상상해보기 완료 이벤트가 발행되면 저장소에 저장하는 메소드
     * @param event 발행된 이벤트
     */
    @Transactional
    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    fun saveEvent(event: ImaginationCompletionEvent){
        repo.save(event)
    }

    /**
     * 이벤트 처리가 완료되면 이벤트의 메시지를 전송하는 메소드
     * 비동기로 동작한다.
     * @param event 처리가 완료된 완료된 이벤트
     */
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun sendEvent(event: ImaginationCompletionEvent){
        messageService.sendMessage(event)
    }
}
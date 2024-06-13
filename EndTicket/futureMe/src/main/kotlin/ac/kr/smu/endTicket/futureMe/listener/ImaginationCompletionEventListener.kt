package ac.kr.smu.endTicket.futureMe.listener

import ac.kr.smu.endTicket.futureMe.domain.event.repository.EventRepository
import ac.kr.smu.endTicket.futureMe.domain.event.model.ImaginationCompletionEvent
import ac.kr.smu.endTicket.futureMe.infra.messaging.ImaginationCompletionEventMessageService
import ac.kr.smu.endTicket.futureMe.service.FutureMeService
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import org.springframework.transaction.event.TransactionPhase
import org.springframework.transaction.event.TransactionalEventListener

/**
 * 상상해보기 완료 이벤트를 처리하는 클래스
 * @property repo 이벤트를 저장하기 위한 저장소
 * @property messageService 메시지를 전송하기 위한 서비스
 * @property futureMeService 상상해보기 완료의 경험치 상승을 위한 서비스
 */
@Component
class ImaginationCompletionEventListener(
    private val repo: EventRepository,
    private val messageService: ImaginationCompletionEventMessageService,
    private val futureMeService: FutureMeService
) {
    private val log = LoggerFactory.getLogger(ImaginationCompletionEventListener::class.java)
    /**
     * 상상해보기 완료 이벤트가 발행되면 저장소에 저장하는 메소드
     * @param event 발행된 이벤트
     */
    @Transactional
    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    fun saveEvent(event: ImaginationCompletionEvent){
        futureMeService.gainExperiencePoints(event)
        repo.save(event)
    }

    /**
     * 이벤트 처리가 완료되면 이벤트의 메시지를 전송하는 메소드, 메시지 전송에 성공하면 이를 기록하고 저장한다.
     * 비동기로 동작한다.
     * @param event 처리가 완료된 완료된 이벤트
     */
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun sendEvent(event: ImaginationCompletionEvent){
        val message = event.toMessage()
        messageService.sendMessage(message).whenCompleteAsync { result, e ->
            if (e == null)
                repo.save(event.also { it.successSend() })
            else
                log.error("key: ${message.key}, payload: ${message.payload}",e)
        }
    }
}
package ac.kr.smu.endTicket.futureme.service

import ac.kr.smu.endTicket.futureme.domain.event.model.Event
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service

/**
 * 이벤트를 발행하는 것을 담당하는 클래스
 * @param eventPublisher 이벤트 발행을 수행하는 객체
 */
@Service
class FutureMeEventService(
    private val eventPublisher: ApplicationEventPublisher
) {
    /**
     * [Event]를 발행하는 메소드
     * @param event 발행할 이벤트
     */
    fun publishEvent(event: Event) = eventPublisher.publishEvent(event)
}
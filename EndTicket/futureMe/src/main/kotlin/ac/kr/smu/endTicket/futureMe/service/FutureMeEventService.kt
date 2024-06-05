package ac.kr.smu.endTicket.futureMe.service

import ac.kr.smu.endTicket.futureMe.domain.event.Event
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service

/**
 * 이벤트를 전송하는것을 담당하는 클래스
 */
@Service
class FutureMeEventService(
    private val eventPublisher: ApplicationEventPublisher
) {
    fun eventPublish(event: Event) = eventPublisher.publishEvent(event)
}
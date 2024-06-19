package ac.kr.smu.endticket.ticket.service

import ac.kr.smu.endticket.ticket.domain.model.TicketCompletedEvent
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service

/**
 * 티켓 완료 이벤트를 관련 기능을 담당하는 서비스
 * @property eventPublisher 이벤트를 발송하기 위한 객체
 */
@Service
class TicketCompletedEventService(
    private val eventPublisher: ApplicationEventPublisher,
) {
    fun publishEvent(event: TicketCompletedEvent) = eventPublisher.publishEvent(event)

}
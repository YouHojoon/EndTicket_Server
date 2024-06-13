package ac.kr.smu.endTicket.ticket.service

import ac.kr.smu.endTicket.ticket.domain.model.TicketCompletionEvent
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service

/**
 * 티켓 완료 이벤트를 관련 기능을 담당하는 서비스
 * @property eventPublisher 이벤트를 발송하기 위한 객체
 */
@Service
class TicketCompletionEventService(
    private val eventPublisher: ApplicationEventPublisher,
) {
    fun eventPublish(event: TicketCompletionEvent){
        eventPublisher.publishEvent(event)
    }
}
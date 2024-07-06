package ac.kr.smu.endticket.user.service

import ac.kr.smu.endticket.user.domain.model.UserDeletedEvent
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Component

@Component
class UserEventService(
    private val eventPublisher: ApplicationEventPublisher,
) {
    fun publish(event: UserDeletedEvent) = eventPublisher.publishEvent(event)
}

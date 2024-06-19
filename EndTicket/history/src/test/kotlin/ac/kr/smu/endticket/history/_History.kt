package ac.kr.smu.endticket.history

import ac.kr.smu.endticket.history.domain.model.TicketHistory
import ac.kr.smu.endticket.history.infra.messaging.TicketCompletedEventResponse

const val USER_ID = 1L
val TICKET_COMPLETED_EVENT_RESPONSE = TicketCompletedEventResponse(
    id = 1L,
    behavior = "behavior",
    target = "target",
    color = TicketHistory.Color.BLUE1,
    type = TicketHistory.Type.HEALTH,
    swipeCount = 5)
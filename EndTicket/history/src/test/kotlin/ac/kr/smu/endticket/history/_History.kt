package ac.kr.smu.endticket.history

import ac.kr.smu.endticket.history.domain.model.Color
import ac.kr.smu.endticket.history.domain.model.TicketHistory
import ac.kr.smu.endticket.history.infra.messaging.ImaginationCompletedEventResponse
import ac.kr.smu.endticket.history.infra.messaging.TicketCompletedEventResponse

const val USER_ID = 1L
val TICKET_COMPLETED_EVENT_RESPONSE = TicketCompletedEventResponse(
    id = 1L,
    behavior = "behavior",
    target = "target",
    color = Color.BLUE1,
    type = TicketHistory.Type.HEALTH,
    swipeCount = 5)

val IMAGINATION_COMPLETED_EVENT_RESPONSE = ImaginationCompletedEventResponse(
    id = 1L,
    behavior = "behavior",
    target = "target",
    color = Color.BLUE1,
    )
package ac.kr.smu.endticket.ticket.event

import ac.kr.smu.endticket.common.web.enum.Color
import ac.kr.smu.endticket.common.web.enum.TicketType
import ac.kr.smu.endticket.ticket.ticket.TicketTestParameters
import ac.kr.smu.endticket.ticket.domain.model.Ticket
import ac.kr.smu.endticket.ticket.domain.model.TicketCompletedEvent
import ac.kr.smu.endticket.ticket.ui.request.TicketRequest
import org.junit.jupiter.params.provider.Arguments
import java.util.stream.Stream

object EventTestParameters {
    const val PATH = "ac.kr.smu.endticket.ticket.event.EventTestParameters"
    const val USER_ID = 1L

    @JvmStatic
    fun provideEvent() = Stream.of(Arguments.of(TicketCompletedEvent(Ticket.from(TICKET_REQUEST, USER_ID))))

    @JvmStatic
    fun provideTicketAndEvent(): Stream<Arguments>{
        val ticket = Ticket.from(TICKET_REQUEST, USER_ID)

        return Stream.of(
            Arguments.of(ticket, TicketCompletedEvent(ticket))
        )
    }


    private val TICKET_REQUEST =
        TicketRequest(
            behavior = "b",
            target = "t",
            color = Color.BLUE1,
            type = TicketType.SELF_IMPROVEMENT,
            maxSwipeCount = Ticket.MaxSwipeCount.FIVE,
        )
}
package ac.kr.smu.endticket.ticket.ticket

import ac.kr.smu.endticket.common.web.enum.Color
import ac.kr.smu.endticket.common.web.enum.TicketType
import ac.kr.smu.endticket.ticket.domain.exception.TicketNotFoundException
import ac.kr.smu.endticket.ticket.domain.exception.TicketOwnershipException
import ac.kr.smu.endticket.ticket.domain.model.Ticket
import ac.kr.smu.endticket.ticket.domain.model.TicketCompletedEvent
import ac.kr.smu.endticket.ticket.ui.request.TicketRequest
import org.junit.jupiter.params.provider.Arguments
import java.util.stream.Stream

object TicketTestParameters {
    const val PATH = "ac.kr.smu.endticket.ticket.ticket.TicketTestParameters"
    const val USER_ID = 1L

    val TICKET_REQUEST =
        TicketRequest(
            behavior = "b",
            target = "t",
            color = Color.BLUE1,
            type = TicketType.SELF_IMPROVEMENT,
            maxSwipeCount = Ticket.MaxSwipeCount.FIVE,
        )

    val UPDATE_REQUEST =
        TicketRequest(
            "abcd",
            "abcd",
            Color.GRAY2,
            TicketType.PERSONALITY,
            Ticket.MaxSwipeCount.FIFTEEN,
        )

    @JvmStatic
    fun provideTicket() =
        Stream.of(
            Arguments.of(Ticket.from(TICKET_REQUEST, USER_ID)),
        )

    @JvmStatic
    fun provideInvalidTicket() =
        Stream.of(
            Arguments.of(Ticket.from(TICKET_REQUEST, USER_ID), 2L, TicketOwnershipException::class),
            Arguments.of(null, USER_ID, TicketNotFoundException::class),
        )

    @JvmStatic
    fun provideInvalidId() =
        Stream.of(
            Arguments.of(1L, 2L, TicketOwnershipException(1L, 2L), 403),
            Arguments.of(1L, USER_ID, TicketNotFoundException(1L), 404),
        )

    @JvmStatic
    fun provideInvalidRequest() =
        Stream.of(
            Arguments.of(
                TicketRequest(
                    "",
                    "new target",
                    Color.GREEN1,
                    TicketType.VALUES,
                    Ticket.MaxSwipeCount.FIFTEEN,
                ),
            ),
            Arguments.of(
                TicketRequest(
                    "new behavior",
                    "",
                    Color.GREEN1,
                    TicketType.VALUES,
                    Ticket.MaxSwipeCount.FIFTEEN,
                ),
            ),
            Arguments.of(
                TicketRequest(
                    "very very long long behavior",
                    "new target",
                    Color.GREEN1,
                    TicketType.VALUES,
                    Ticket.MaxSwipeCount.FIFTEEN,
                ),
            ),
            Arguments.of(
                TicketRequest(
                    "behavior",
                    "very very long long target",
                    Color.GREEN1,
                    TicketType.VALUES,
                    Ticket.MaxSwipeCount.FIFTEEN,
                ),
            ),
        )

    @JvmStatic
    fun provideEvent() = Stream.of(Arguments.of(TicketCompletedEvent(Ticket.from(TICKET_REQUEST, USER_ID))))
}

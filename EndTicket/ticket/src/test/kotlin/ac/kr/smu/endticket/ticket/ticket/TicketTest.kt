package ac.kr.smu.endticket.ticket.ticket

import ac.kr.smu.endticket.ticket.domain.exception.TicketOwnershipException
import ac.kr.smu.endticket.ticket.domain.model.Ticket
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TicketTest {
    @ParameterizedTest
    @DisplayName("티켓 수정 테스트")
    @MethodSource("${TicketTestParameters.PATH}#provideTicket")
    fun given_ticketRequest_when_updateAndCheckCompletion_then_updateTicketAndReturnIsCompletion(ticket: Ticket) {
        val request = TicketTestParameters.UPDATE_REQUEST
        ticket.updateAndCheckCompletion(request, TicketTestParameters.USER_ID)

        val response = ticket.toResponse()

        assertEquals(request.behavior, response.behavior)
        assertEquals(request.target, response.target)
        assertEquals(request.color, response.color)
        assertEquals(request.maxSwipeCount, response.maxSwipeCount)
        assertEquals(request.type, response.type)
    }

    @ParameterizedTest
    @DisplayName("소유자가 아닌 사용자의 수정 요청 테스트")
    @MethodSource("${TicketTestParameters.PATH}#provideTicket")
    fun given_userWhoNotOwnerOfTicket_when_updateAndCheckCompletion_then_throwTicketOwnershipException(ticket: Ticket) {
        assertThrows<TicketOwnershipException> {
            ticket.updateAndCheckCompletion(
                TicketTestParameters.UPDATE_REQUEST,
                2L,
            )
        }
    }

    @ParameterizedTest
    @DisplayName("티켓 스와이프 테스트")
    @MethodSource("${TicketTestParameters.PATH}#provideTicket")
    fun given_userId_when_swipeTicket_then_plusSwipeCount_and_returnIsComplete(ticket: Ticket) {
        val beforeSwipeCount = ticket.swipeCount

        assertFalse(ticket.swipeAndCheckCompletion(TicketTestParameters.USER_ID))
        assertEquals(beforeSwipeCount + 1, ticket.swipeCount)
    }

    @ParameterizedTest
    @DisplayName("소유자가 아닌 사용자의 티켓 스와이프 테스트")
    @MethodSource("${TicketTestParameters.PATH}#provideTicket")
    fun given_userWhoNotOwnerOfTicket_when_swipeTicket_then_throwTicketOwnershipException(ticket: Ticket) {
        assertThrows<TicketOwnershipException> { ticket.swipeAndCheckCompletion(2L) }
    }

    @ParameterizedTest
    @DisplayName("최대 스와이프 횟수 이상으로 스와이프 테스트")
    @MethodSource("${TicketTestParameters.PATH}#provideTicket")
    fun given_ticketReachedMaxSwipeCount_when_swipeTicket_then_nothingChange(ticket: Ticket) {
        repeat(TicketTestParameters.TICKET_REQUEST.maxSwipeCount.value) {
            ticket.swipeAndCheckCompletion(TicketTestParameters.USER_ID)
        }

        val beforeSwipeCount = ticket.swipeCount

        assertTrue(ticket.swipeAndCheckCompletion(TicketTestParameters.USER_ID))
        assertEquals(beforeSwipeCount, ticket.swipeCount)
    }

    @ParameterizedTest
    @DisplayName("티켓 스와이프 취소 테스트")
    @MethodSource("${TicketTestParameters.PATH}#provideTicket")
    fun given_userId_when_cancelSwipeTicket_then_minusSwipeCount(ticket: Ticket) {
        ticket.swipeAndCheckCompletion(TicketTestParameters.USER_ID)
        ticket.cancelSwipeTicket(TicketTestParameters.USER_ID)

        assertEquals(0, ticket.swipeCount)
    }

    @ParameterizedTest
    @DisplayName("소유자가 아닌 사용자의 티켓 스와이프 취소 테스트")
    @MethodSource("${TicketTestParameters.PATH}#provideTicket")
    fun given_userWhoNotOwnerOfTicket_when_cancelSwipeTicket_then_throwTicketOwnershipException(ticket: Ticket) {
        ticket.swipeAndCheckCompletion(TicketTestParameters.USER_ID)
        assertThrows<TicketOwnershipException> { ticket.cancelSwipeTicket(2L) }
    }

    @ParameterizedTest
    @DisplayName("0회 이하로 티켓 스와이프 취소 테스트")
    @MethodSource("${TicketTestParameters.PATH}#provideTicket")
    fun given_ticketWithSwipeCountZero_when_cancelSwipeTicket_then_nothingChange(ticket: Ticket) {
        ticket.cancelSwipeTicket(TicketTestParameters.USER_ID)

        assertEquals(0, ticket.swipeCount)
    }
}

package ac.kr.smu.endTicket.ticket

import ac.kr.smu.endTicket.ticket.domain.exception.NotOwnerOfTicketException
import ac.kr.smu.endTicket.ticket.domain.model.Ticket
import ac.kr.smu.endTicket.ticket.ui.request.TicketRequest
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TicketTest {
    @Test
    @DisplayName("티켓 수정 테스트")
    fun given_ticketRequest_when_updateAndCheckCompletion_then_updateTicket_and_returnIsCompletion(){
        val ticket = Ticket.from(TICKET_REQUEST, USER_ID)
        val updateRequest = TicketRequest(
            "abcd",
            "abcd",
            Ticket.Color.GRAY2,
            Ticket.Type.PERSONALITY,
            Ticket.MaxSwipeCount.FIFTEEN
        )

        ticket.updateAndCheckCompletion(updateRequest, USER_ID)
        assertEquals(Ticket.from(updateRequest, USER_ID), ticket)
        assertTrue(ticket.shouldUpdate)
    }

    @Test
    @DisplayName("소유자가 아닌 사용자의 수정 요청 테스트")
    fun given_userWhoNotOwnerOfTicket_when_updateAndCheckCompletion_then_throwNotOwnerOfTicket(){
        val ticket = Ticket.from(TICKET_REQUEST, USER_ID)

        assertThrows<NotOwnerOfTicketException> {  ticket.updateAndCheckCompletion(UPDATE_REQUEST, 2L)}
    }

    @Test
    @DisplayName("티켓 스와이프 테스트")
    fun given_userID_when_swipeTicket_then_plusSwipeCount_and_returnIsComplete(){
        val ticket = Ticket.from(TICKET_REQUEST, USER_ID)
        val beforeSwipeCount = ticket.swipeCount

        assertFalse(ticket.swipeAndCheckCompletion(USER_ID))
        assertEquals(beforeSwipeCount + 1, ticket.swipeCount)
        assertTrue(ticket.shouldUpdate)

    }

    @Test
    @DisplayName("소유자가 아닌 사용자의 티켓 스와이프 테스트")
    fun given_userWhoNotOwnerOfTicket_when_swipeTicket_then_throwNotOwnerOfTicketException(){
        val ticket = Ticket.from(TICKET_REQUEST, USER_ID)
        assertThrows<NotOwnerOfTicketException> {  ticket.swipeAndCheckCompletion(2L)}
    }

    @Test
    @DisplayName("최대 스와이프 횟수 이상으로 스와이프 테스트")
    fun given_ticketReachedMaxSwipeCount_when_swipeTicket_then_nothingChange(){
        val ticket = Ticket.from(TICKET_REQUEST, USER_ID)

        repeat(ticket.maxSwipeCount.value){
            ticket.swipeAndCheckCompletion(USER_ID)
        }

        val beforeSwipeCount = ticket.swipeCount

        assertTrue(ticket.swipeAndCheckCompletion(USER_ID))
        assertEquals(beforeSwipeCount, ticket.swipeCount)
        assertTrue(ticket.shouldUpdate)
    }

    @Test
    @DisplayName("티켓 스와이프 취소 테스트")
    fun given_userID_when_cancelSwipeTicket_then_minusSwipeCount(){
        val ticket = Ticket.from(TICKET_REQUEST, USER_ID)

        ticket.swipeAndCheckCompletion(USER_ID)
        ticket.cancelSwipeTicket(USER_ID)

        assertTrue(ticket.shouldUpdate)
        assertEquals(0, ticket.swipeCount)
    }

    @Test
    @DisplayName("소유자가 아닌 사용자의 티켓 스와이프 취소 테스트")
    fun given_userWhoNotOwnerOfTicket_when_cancelSwipeTicket_then_throwNotOwnerOfTicketException(){
        val ticket = Ticket.from(TICKET_REQUEST, USER_ID)

        ticket.swipeAndCheckCompletion(USER_ID)
        assertThrows<NotOwnerOfTicketException> {  ticket.cancelSwipeTicket(2L)}
    }

    @Test
    @DisplayName("0회 이하로 티켓 스와이프 취소 테스트")
    fun given_ticketWithSwipeCountZero_when_cancelSwipeTicket_then_nothingChange(){
        val ticket = Ticket.from(TICKET_REQUEST, USER_ID)
        ticket.cancelSwipeTicket(USER_ID)

        assertEquals(0,ticket.swipeCount)
        assertFalse(ticket.shouldUpdate)
    }
}
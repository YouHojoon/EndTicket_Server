package ac.kr.smu.endTicket.ticket.domain.exception

/**
 * 티켓의 소유자가 아닌 사용자가 티켓 수정 요청을 할 시 발생하는 에러
 */
class NotOwnerOfTicketException: RuntimeException()
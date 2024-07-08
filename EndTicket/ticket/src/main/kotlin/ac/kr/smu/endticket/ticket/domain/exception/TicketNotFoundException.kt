package ac.kr.smu.endticket.ticket.domain.exception

/**
 * 티켓이 존재하지 않을 때 발생하는 에러
 */
class TicketNotFoundException : RuntimeException("티켓이 존재하지 않습니다.")

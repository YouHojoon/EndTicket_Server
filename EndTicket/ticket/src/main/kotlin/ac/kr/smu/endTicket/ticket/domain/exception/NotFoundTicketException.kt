package ac.kr.smu.endTicket.ticket.domain.exception

/**
 * 티켓이 존재하지 않을 때 발생하는 에러
 * @property id 존재하지 않는 티켓의 id
 */
data class NotFoundTicketException(
    val id: Long
) : RuntimeException("$id 인 티켓을 찾을 수 없습니다.")
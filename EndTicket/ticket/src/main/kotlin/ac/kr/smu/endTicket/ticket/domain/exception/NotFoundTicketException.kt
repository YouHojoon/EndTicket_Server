package ac.kr.smu.endTicket.ticket.domain.exception

data class NotFoundTicketException(
    val id: Long
) : RuntimeException("$id 인 티켓을 찾을 수 없습니다.")
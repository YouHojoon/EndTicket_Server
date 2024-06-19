package ac.kr.smu.endTicket.ticket.domain.exception

/**
 * 티켓의 소유자가 아닌 사용자가 티켓에 대한 요청을 할 시 발생하는 에러
 * @property id 티켓의 ID
 * @property userID 요청을 보낸 사용자의 ID
 */
class TicketOwnershipException(
    val id: Long,
    val userID: Long,
): RuntimeException("$userID 사용자는 $id 티켓의 소유자가 아닙니다.")
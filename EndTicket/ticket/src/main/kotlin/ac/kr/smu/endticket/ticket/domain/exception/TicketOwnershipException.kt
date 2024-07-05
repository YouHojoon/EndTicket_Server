package ac.kr.smu.endticket.ticket.domain.exception

/**
 * 티켓의 소유자가 아닌 사용자가 티켓에 대한 요청을 할 시 발생하는 에러
 * @property id 티켓의 id
 * @property userId 요청을 보낸 사용자의 id
 */
class TicketOwnershipException(
    val id: Long,
    val userId: Long,
) : RuntimeException("$userId 사용자는 $id 티켓의 소유자가 아닙니다.")

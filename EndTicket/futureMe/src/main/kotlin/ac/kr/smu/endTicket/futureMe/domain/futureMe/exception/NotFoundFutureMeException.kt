package ac.kr.smu.endTicket.futureMe.domain.futureMe.exception

/**
 * @param userID 미래의 나가 존재하지 않는 userID
 */
class NotFoundFutureMeException(
    private val userID: Long
): RuntimeException("$userID 의 미래의 나가 존재하지 않습니다.")
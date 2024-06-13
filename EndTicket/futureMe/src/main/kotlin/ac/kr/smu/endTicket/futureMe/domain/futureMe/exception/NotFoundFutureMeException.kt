package ac.kr.smu.endTicket.futureMe.domain.futureMe.exception

/**
 * 미래의 나가 존재하지 않을 때 발생하는 Exception
 * @param userID 미래의 나가 존재하지 않는 userID
 */
class NotFoundFutureMeException(
    val userID: Long
): RuntimeException("$userID 의 미래의 나가 존재하지 않습니다.")
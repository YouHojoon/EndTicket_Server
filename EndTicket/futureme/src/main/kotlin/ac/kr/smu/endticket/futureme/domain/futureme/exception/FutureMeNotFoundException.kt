package ac.kr.smu.endticket.futureme.domain.futureme.exception

/**
 * 미래의 나가 존재하지 않을 때 발생하는 Exception
 * @param userId 미래의 나가 존재하지 않는 userId
 */
class FutureMeNotFoundException(
    val userId: Long,
) : RuntimeException("$userId 의 미래의 나가 존재하지 않습니다.")

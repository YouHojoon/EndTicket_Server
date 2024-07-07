package ac.kr.smu.endTicket.auth.domain.exception

/**
 * 만료된 사용자일 때 발생하는 에러
<<<<<<< HEAD
 * @property userId 만료된 사용자의 id
 */
class UserExpiredException(
    val userId: Long,
) : RuntimeException("$userId 인 사용자는 만료되었습니다.")
=======
 * @param userId 만료된 사용자의 id
 */
class UserExpiredException(
    userId: Long
): RuntimeException("$userId 인 사용자는 만료되었습니다.")
>>>>>>> 4095bee1ea1d771194231947fa1cd3287c66d5dc

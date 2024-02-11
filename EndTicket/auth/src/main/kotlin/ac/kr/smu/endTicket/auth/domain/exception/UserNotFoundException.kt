package ac.kr.smu.endTicket.auth.domain.exception

/**
 * socialUserNumber로 사용자를 찾을 수 없을 때 발생하는 Exception
 */
class UserNotFoundException(val socialUserNumber: Long): RuntimeException() {
}
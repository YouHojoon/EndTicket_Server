package ac.kr.smu.endTicket.auth.domain.exception

/**
 * SNS 사용자 번호로 사용자를 찾을 수 없을 때 발생하는 Exception
 * @property socialUserNumber SNS 사용자 번호
 */
class UserNotFoundException(val socialUserNumber: String): RuntimeException() {
}
package ac.kr.smu.endTicket.user.domain.exception

/**
 * id를 이용해 사용자를 찾지 못했을 떄 발생하는 에러
 * @property id 사용자를 찾지 못한 id
 */
class NotFoundUserException(val id: Long): RuntimeException()
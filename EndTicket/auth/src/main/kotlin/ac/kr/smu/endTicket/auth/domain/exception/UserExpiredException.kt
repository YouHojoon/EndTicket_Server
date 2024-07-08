package ac.kr.smu.endTicket.auth.domain.exception

/**
 * 만료된 사용자일 때 발생하는 에러
 */
class UserExpiredException: RuntimeException("만료된 사용자입니다.")



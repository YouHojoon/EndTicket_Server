package ac.kr.smu.endTicket.infra.oAuth.IDToken.exception

/**
 * ID 토큰이 검증되지 않았을 시 발생하는 Exception
 */
class IDTokenNotVerifyException(message: String?): RuntimeException(message)
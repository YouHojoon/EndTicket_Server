package ac.kr.smu.endTicket.infra.oAuth.IDToken.exception


/**
 * ID 토큰을 검증하는 공개키를 받아오는 데 실패했을 시 발생하는 Exception
 */
class JWKParseException(message:String?, cause: Throwable?): RuntimeException(message, cause)
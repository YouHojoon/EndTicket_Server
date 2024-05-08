package ac.kr.smu.endTicket.auth.infra.oauth2.idToken.exception


/**
 * ID 토큰을 검증하는 공개키를 받아오는 데 실패했을 시 발생하는 Exception
 * @property clientName 공개키를 받는데 실패한 client 이름
 * @property message SNS 서버에서 반환받은 에러 메시지
 * @property cause 원래의 에러
 */
class JWKParseException(
    val clientName: String,
    message:String? = null,
    cause: Throwable? = null
): RuntimeException(message, cause)
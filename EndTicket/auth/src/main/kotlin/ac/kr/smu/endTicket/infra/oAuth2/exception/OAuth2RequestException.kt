package ac.kr.smu.endTicket.infra.oAuth2.exception

/**
 * OAuth 요청이 실패했을 시 발생하는 Exception
 * @property message 실패 메시지
 * @property cause 원래의 에러
 */
class OAuth2RequestException(message: String?, cause: Throwable?): RuntimeException(message, cause)
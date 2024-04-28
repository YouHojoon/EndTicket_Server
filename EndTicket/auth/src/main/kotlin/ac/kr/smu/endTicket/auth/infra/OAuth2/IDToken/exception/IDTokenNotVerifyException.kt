package ac.kr.smu.endTicket.auth.infra.OAuth2.IDToken.exception

import ac.kr.smu.endTicket.auth.domain.model.SocialType


/**
 * ID 토큰이 검증되지 않았을 시 발생하는 Exception
 * @property idToken 검증에 실패한 ID 토큰
 * @property message 에러 메시지
 */
class IDTokenNotVerifyException(
    val socialType: SocialType,
    val idToken: String,
    message: String?): RuntimeException(message)
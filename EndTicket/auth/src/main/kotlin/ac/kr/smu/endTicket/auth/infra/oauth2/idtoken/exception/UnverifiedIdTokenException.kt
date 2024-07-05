package ac.kr.smu.endTicket.auth.infra.oauth2.idtoken.exception

import ac.kr.smu.endTicket.auth.domain.model.SocialType

/**
 * ID 토큰이 검증되지 않았을 시 발생하는 Exception
 * @property socialType SNS 타입
 * @property idToken 검증에 실패한 ID 토큰
 * @property message 에러 메시지
 */
class UnverifiedIdTokenException(
    val socialType: SocialType,
    val idToken: String,
    message: String? = null,
) : RuntimeException(message)

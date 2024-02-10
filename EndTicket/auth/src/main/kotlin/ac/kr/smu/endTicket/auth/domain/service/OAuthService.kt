package ac.kr.smu.endTicket.auth.domain.service

import ac.kr.smu.endTicket.auth.domain.model.SocialType
import ac.kr.smu.endTicket.infra.oAuth.OAuthTokenResponse


/**
 * 외부 SNS 서비스의 인증 기능을 위한 클래스
 */
interface OAuthService {
    /**
     * 외부 SNS 서비스 인증을 하여 access 토큰 응답을 반환하는 메소드
     * @param SNS SNS 종류
     * @param code SNS 인증에서 반환받은 authorization code
     */
    fun oAuth(SNS: SocialType, code: String): OAuthTokenResponse
    fun parseSocialUserNumber(SNS: SocialType, idToken: String): Long
}
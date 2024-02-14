package ac.kr.smu.endTicket.auth.domain.service

import ac.kr.smu.endTicket.auth.domain.model.SocialType
import ac.kr.smu.endTicket.infra.oAuth2.OAuth2TokenResponse


/**
 * 외부 SNS 서비스의 인증 기능을 위한 클래스
 */
interface OAuthService {
    /**
     * 외부 SNS 서비스 인증을 하여 access 토큰 응답을 반환하는 메소드
     * @param socialType SNS 종류
     * @param code SNS 인증에서 반환받은 authorization code
     * @return access token 응답
     */
    fun oAuth(socialType: SocialType, code: String): OAuth2TokenResponse

    /**
     * idToken에서 SNS 사용자 번호를 반환하는 메소드
     * @param socialType SNS 종류
     * @param idToken idToken
     * @return 파싱된 회원 번호
     */
    fun parseSocialUserNumber(socialType: SocialType, idToken: String): Long
}
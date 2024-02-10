package ac.kr.smu.endTicket.auth.service

import ac.kr.smu.endTicket.auth.domain.model.SocialType
import ac.kr.smu.endTicket.auth.domain.service.OAuthService

import org.springframework.stereotype.Service
import java.security.PublicKey

/**
 * 인증 기능을 처리하는 클래스
 * @property oAuthService oAuth 기능을 담당하는 객체
 */
@Service
class AuthService(
    private val oAuthService: OAuthService,
) {
    /**
     * 인증 기능
     * @param SNS 인증에 사용할 SNS
     * @param code 해당 SNS에서 발급받은 authorization code
     * @return JWT 토큰 발급
     */
    fun auth(SNS: SocialType, code: String){
        //todo: oAuth를 활용해 토큰을 응답받고 ID 토큰으로 유저 식별 후 JWT 토큰 발급
        val idToken = oAuthService.oAuth(SNS,code).idToken
        val socialUserNumber = oAuthService.parseSocialUserNumber(SNS, idToken)
        println(socialUserNumber)
    }
}
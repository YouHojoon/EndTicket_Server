package ac.kr.smu.endTicket.auth.service

import ac.kr.smu.endTicket.auth.domain.exception.UserNotFoundException
import ac.kr.smu.endTicket.auth.domain.model.JWTToken
import ac.kr.smu.endTicket.auth.domain.model.SocialType
import ac.kr.smu.endTicket.auth.domain.service.OAuthService
import ac.kr.smu.endTicket.infra.openfeign.UserClient
import feign.FeignException
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value

import org.springframework.stereotype.Service

/**
 * 인증 기능을 처리하는 클래스
 * @property oAuthService oAuth 기능을 담당하는 객체
 */
@Service
class AuthService(
    private val oAuthService: OAuthService,
    private val userClient: UserClient,

    @Value("\${jwt.secret}")
    private val jwtSecret: String
) {

    private val key = Keys.hmacShaKeyFor(jwtSecret.toByteArray())
    /**
     * 인증 기능
     * @param SNS 인증에 사용할 SNS
     * @param code 해당 SNS에서 발급받은 authorization code
     * @return JWT 토큰 발급
     * @throws 해당 SNS로 가입한 적 없을 시
     */
    fun auth(SNS: SocialType, code: String): JWTToken{
        //todo: oAuth를 활용해 토큰을 응답받고 ID 토큰으로 유저 식별 후 JWT 토큰 발급
        val idToken = oAuthService.oAuth(SNS,code).idToken
        val socialUserNumber = oAuthService.parseSocialUserNumber(SNS, idToken)

        try {
            val userID = userClient.getUserId(SNS, socialUserNumber)
            val jwtToken = JWTToken(userID, key)

            return jwtToken
        }catch (e: FeignException.NotFound){
            throw UserNotFoundException(socialUserNumber)
        }
    }
}
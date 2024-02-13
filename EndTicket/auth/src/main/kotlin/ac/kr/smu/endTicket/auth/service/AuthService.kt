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
 * @property oAuthService oAuth 기능을 담당하는 객체,
 * @property userClient 유저 ID를 조회하기 위한 feign client
 * @property jwtSecret jwt 암호화에 사용하기 위한 문자열
 * @property accessTokenExpiration access token 만료 시간
 * @property refreshTokenExpiration refresh token 만료 시간
 */
@Service
class AuthService(
    private val oAuthService: OAuthService,
    private val userClient: UserClient,

    @Value("\${jwt.secret}")
    private val jwtSecret: String,

    @Value("\${jwt.access-token-expiration}")
    private val accessTokenExpiration: Long,

    @Value("\${jwt.refresh-token-expiration}")
    private val refreshTokenExpiration: Long
) {
    /**
     * JWT를 서명하기 위한 key
     */
    private val key = Keys.hmacShaKeyFor(jwtSecret.toByteArray())
    /**
     * 인증 기능
     * @param SNS 인증에 사용할 SNS
     * @param code 해당 SNS에서 발급받은 authorization code
     * @return JWT 토큰 발급
     * @throws UserNotFoundException 해당 SNS로 가입한 적 없을 시
     */
    fun createToken(SNS: SocialType, code: String): JWTToken{
        //todo: oAuth를 활용해 토큰을 응답받고 ID 토큰으로 유저 식별 후 JWT 토큰 발급
        val idToken = oAuthService.oAuth(SNS,code).idToken
        val socialUserNumber = oAuthService.parseSocialUserNumber(SNS, idToken)

        try {
            val response = userClient.getUserId(SNS, socialUserNumber)

            return JWTToken(response.userID, key, accessTokenExpiration, refreshTokenExpiration)
        } catch (e: FeignException.NotFound) {
            throw UserNotFoundException(socialUserNumber)
        }
    }
}
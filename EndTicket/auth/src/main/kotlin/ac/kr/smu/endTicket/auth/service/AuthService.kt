package ac.kr.smu.endTicket.auth.service

import ac.kr.smu.endTicket.auth.domain.exception.UserNotFoundException
import ac.kr.smu.endTicket.auth.domain.model.JWTToken
import ac.kr.smu.endTicket.auth.domain.model.SocialType
import ac.kr.smu.endTicket.auth.domain.service.OAuthService
import ac.kr.smu.endTicket.infra.config.JWTProperties
import ac.kr.smu.endTicket.infra.openfeign.UserClient
import feign.FeignException
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.redis.core.RedisTemplate

import org.springframework.stereotype.Service
import java.util.concurrent.TimeUnit

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
    private val redisTemplate: RedisTemplate<String, String>,
    private val jwtProperties: JWTProperties
) {
    /**
     * JWT를 서명하기 위한 key
     */
    private val key = Keys.hmacShaKeyFor(jwtProperties.secret.toByteArray())

    /**
     * Refresh 토큰이 redis에 저장될 때 키에 붙는 접미사
     */
    private val REDIS_KEY_POSTFIX_FOR_REFRESH_TOKEN = "_refresh_token"

    /**
     * 토큰 생성 기능
     * @param socialType 토큰 생성에 사용할 SNS
     * @param code 해당 SNS에서 발급받은 authorization code
     * @return JWT 토큰 발급
     * @throws UserNotFoundException 해당 SNS로 가입한 적 없을 시
     */
    @Throws(UserNotFoundException::class)
    fun createToken(socialType:  SocialType, code: String): JWTToken{
        val idToken = oAuthService.oAuth(socialType,code).idToken
        val socialUserNumber = oAuthService.parseSocialUserNumber(socialType, idToken)

        try {
            val response = userClient.getUserId(socialType, socialUserNumber)
            val token = JWTToken(response.userID, key, jwtProperties.accessTokenExpiration, jwtProperties.refreshTokenExpiration)
            redisTemplate.opsForValue().set("${response.userID}" + REDIS_KEY_POSTFIX_FOR_REFRESH_TOKEN, token.refreshToken, jwtProperties.refreshTokenExpiration, TimeUnit.MILLISECONDS)

            return token
        } catch (e: FeignException.NotFound) {
            throw UserNotFoundException(socialUserNumber)
        }
    }
}
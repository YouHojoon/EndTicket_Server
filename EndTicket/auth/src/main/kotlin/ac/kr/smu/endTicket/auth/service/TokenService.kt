package ac.kr.smu.endTicket.auth.service

import ac.kr.smu.endTicket.auth.domain.exception.ExpiredTokenException
import ac.kr.smu.endTicket.auth.domain.exception.TokenSignatureException
import ac.kr.smu.endTicket.auth.domain.exception.UserNotFoundException
import ac.kr.smu.endTicket.auth.domain.model.JWTToken
import ac.kr.smu.endTicket.auth.domain.model.SocialType
import ac.kr.smu.endTicket.auth.domain.service.OAuthService
import ac.kr.smu.endTicket.infra.config.JWTProperties
import ac.kr.smu.endTicket.infra.openfeign.UserClient
import feign.FeignException
import io.jsonwebtoken.ExpiredJwtException
import io.jsonwebtoken.Jwe
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import io.jsonwebtoken.security.SignatureException
import org.springframework.data.redis.core.RedisTemplate

import org.springframework.stereotype.Service
import java.util.concurrent.TimeUnit

/**
 * 토큰 기능을 처리하는 클래스
 * @property oAuthService oAuth 기능을 담당하는 객체,
 * @property userClient 유저 ID를 조회하기 위한 feign client
 * @property redisTemplate redis를 사용하기 위한 객체
 * @property jwtProperties JWT 토큰 관련 설정
 */
@Service
class TokenService(
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
     * @param socialType 인증을 한 SNS
     * @param socialUserNumber SNS의 사용자 번호
     * @return JWT 토큰 발급
     */
    @Throws(UserNotFoundException::class)
    fun createToken(socialType:SocialType,socialUserNumber: String): JWTToken{
        try {
            val response = userClient.getUserId(socialType, socialUserNumber)
            val token =  JWTToken(
                userID = response.userID,
                key = key,
                accessTokenExpiration = jwtProperties.accessTokenExpiration,
                refreshTokenExpiration = jwtProperties.refreshTokenExpiration
            )

            redisTemplate
                .opsForValue()
                .set("${response.userID}" + REDIS_KEY_POSTFIX_FOR_REFRESH_TOKEN,
                    token.refreshToken, jwtProperties.refreshTokenExpiration,
                    TimeUnit.MILLISECONDS)

            return token
        } catch (e: FeignException.NotFound) {
            throw UserNotFoundException(socialUserNumber)
        }
    }

    /**
     * access 토큰에서 사용자 ID를 파싱하는 메소드
     * @param token access token
     * @return 사용자 ID
     * @throws ExpiredTokenException 토큰이 만료되었을 시 발생하는 Exception
     * @throws TokenSignatureException 토큰의 서명이 잘못되었을 시 발생하는 Exception
     */
    @Throws(ExpiredTokenException::class, SignatureException::class)
    fun parseUserID(token: String): Long{
        try {
            val claims = Jwts
                .parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)

            return claims.payload.subject.toLong()
        }catch (e: ExpiredJwtException){
            throw ExpiredTokenException()
        }
        catch (e: SignatureException){
            throw TokenSignatureException()
        }
    }
}
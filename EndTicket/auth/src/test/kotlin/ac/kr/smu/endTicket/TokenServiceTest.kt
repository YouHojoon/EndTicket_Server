package ac.kr.smu.endTicket

import ac.kr.smu.endTicket.auth.domain.exception.UserNotFoundException
import ac.kr.smu.endTicket.auth.domain.model.SocialType
import ac.kr.smu.endTicket.auth.domain.service.OAuthService
import ac.kr.smu.endTicket.auth.service.TokenService
import ac.kr.smu.endTicket.infra.config.JWTProperties

import ac.kr.smu.endTicket.infra.oAuth2.OAuth2TokenResponse
import ac.kr.smu.endTicket.infra.openfeign.GetUserIDResponse
import ac.kr.smu.endTicket.infra.openfeign.UserClient
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow

import feign.FeignException
import feign.Request
import feign.RequestTemplate
import org.junit.jupiter.api.*

import org.mockito.Mock
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean

import org.springframework.data.redis.core.RedisTemplate
import org.springframework.test.context.ActiveProfiles

@SpringBootTest
@ActiveProfiles("test")
@EnableConfigurationProperties(JWTProperties::class)
class TokenServiceTest(
){
    @MockBean
    private lateinit var oAuthService: OAuthService
    @MockBean
    private lateinit var userClient: UserClient
    @Mock
    private lateinit var redisTemplate: RedisTemplate<String, String>
    @Autowired
    private lateinit var service: TokenService

    @BeforeEach
    fun setUp(){
        Mockito.`when`(oAuthService.oAuth(SocialType.KAKAO, "1"))
            .thenReturn(OAuth2TokenResponse("jwt","a","i",1,"1","1",""))
        Mockito.`when`(oAuthService.parseSocialUserNumber(SocialType.KAKAO, "i"))
            .thenReturn("1")
        Mockito.`when`(userClient.getUserId(SocialType.KAKAO, "1"))
            .thenReturn(GetUserIDResponse(1))
    }

    @Test
    @DisplayName("정상 유저 토큰 발급 테스트")
    fun given_normal_user_then_success_createToken(){
        assertDoesNotThrow {
            service.createAccessAndRefreshToken(SocialType.KAKAO, "1")
        }
    }

    @Test
    @DisplayName("미가입 유저 토큰 발급 테스트")
    fun given_notSignUp_user_then_throw_UserNotFoundException(){
        Mockito.`when`(oAuthService.oAuth(SocialType.KAKAO, "2"))
            .thenReturn(OAuth2TokenResponse("jwt","a","i",1,"1","1",""))
        Mockito.`when`(oAuthService.parseSocialUserNumber(SocialType.KAKAO, "i"))
            .thenReturn("2")
        Mockito.`when`(userClient.getUserId(SocialType.KAKAO, "2"))
            .thenAnswer {
                throw FeignException.NotFound(
                    "message",
                    Request.create(Request.HttpMethod.GET,
                        "",
                        emptyMap(),
                        Request.Body.empty(),
                        RequestTemplate()),
                    ByteArray(0),
                    emptyMap()
                )
            }

        assertThrows<UserNotFoundException> {
            service.createAccessAndRefreshToken(SocialType.KAKAO,"2")
        }
    }

    @Test
    @DisplayName("access 토큰으로 사용자 인증")
    fun given_normal_accessToken_then_success_validToken() {
        val token = service
            .createAccessAndRefreshToken(SocialType.KAKAO, "1")

        assertDoesNotThrow {
            service.parseUserID(token.accessToken)
        }
    }
}



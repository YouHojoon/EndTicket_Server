package ac.kr.smu.endTicket

import ac.kr.smu.endTicket.auth.domain.model.SocialType
import ac.kr.smu.endTicket.auth.domain.service.OAuthService
import ac.kr.smu.endTicket.auth.service.AuthService
import ac.kr.smu.endTicket.infra.config.JWTProperties
import ac.kr.smu.endTicket.infra.oAuth2.OAuth2TokenResponse
import ac.kr.smu.endTicket.infra.openfeign.GetUserIDResponse
import ac.kr.smu.endTicket.infra.openfeign.UserClient
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.mockito.Mock
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.data.redis.core.RedisTemplate

@SpringBootTest
@EnableConfigurationProperties(JWTProperties::class)
class AuthServiceTest(
){
    @MockBean
    private lateinit var oAuthService: OAuthService
    @MockBean
    private lateinit var userClient: UserClient
    @Mock
    private lateinit var redisTemplate: RedisTemplate<String, String>

    @Autowired
    private lateinit var jwtProperties: JWTProperties
    @Autowired
    private lateinit var service: AuthService


    @Test
    @DisplayName("토큰 발급 테스트")
    fun given_normal_user_then_success_createToken(){
        Mockito.`when`(oAuthService.oAuth(SocialType.KAKAO, "1"))
            .thenReturn(OAuth2TokenResponse("jwt","a","i",1,"1","1",""))
        Mockito.`when`(oAuthService.parseSocialUserNumber(SocialType.KAKAO, "i"))
            .thenReturn(1)
        Mockito.`when`(userClient.getUserId(SocialType.KAKAO, 1))
            .thenReturn(GetUserIDResponse(1))

        assertDoesNotThrow {
            println( service.createToken(SocialType.KAKAO, "1"))
        }
    }
}

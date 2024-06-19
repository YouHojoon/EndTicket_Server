package ac.kr.smu.endTicket.auth

import ac.kr.smu.endTicket.auth.domain.service.OAuthService
import ac.kr.smu.endTicket.auth.service.UserService
import ac.kr.smu.endTicket.auth.infra.oauth2.OAuth2TokenResponse
import ac.kr.smu.endTicket.auth.infra.property.JWTProperties
import ac.kr.smu.endTicket.auth.service.TokenService
import ac.kr.smu.endTicket.auth.ui.controller.AuthController
import ac.kr.smu.endTicket.auth.ui.response.TokenResponse
import ac.kr.smu.endticket.common.redis.config.AutoRedisConfig
import ac.kr.smu.endticket.common.redis.test.RedisTestConfig
import ac.kr.smu.endticket.common.web.test.andReturn
import ac.kr.smu.endticket.common.web.test.expectExceptionResponse
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.context.annotation.Import
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.http.MediaType
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.test.web.servlet.setup.StandaloneMockMvcBuilder
import org.springframework.web.context.WebApplicationContext
import java.lang.IllegalStateException
import kotlin.test.assertNotNull

@SpringBootTest(
    classes = [
        AuthController::class,
        TokenService::class,
        RedisAutoConfiguration::class,
        WebMvcAutoConfiguration::class
    ]
)
@Import(RedisTestConfig::class, SecurityTestConfig::class, AutoRedisConfig::class)
@EnableConfigurationProperties(JWTProperties::class)
@AutoConfigureMockMvc
class AuthIntegrationTest @Autowired constructor(
    @MockBean
    private val oAuthService: OAuthService,
    @MockBean
    private val userService: UserService,
    private val redisTemplate: RedisTemplate<String, Any>,
    private val mvc: MockMvc
) {
    @BeforeEach
    fun init(){
        mockOAuthService()
    }

    @AfterEach
    fun reset(){
        redisTemplate.connectionFactory?.connection?.let {
            it.serverCommands().flushAll()
        }
    }

    @Test
    @DisplayName("사용자 토큰 생성 테스트")
    fun given_user_when_createToken_then_responseAccessToken_and_refreshToken(){
        Mockito.`when`(userService.findUserId(SOCIAL_TYPE, SOCIAL_USER_NUMBER))
            .thenReturn(USER_ID)

        mvc.createToken()
            .andExpect(MockMvcResultMatchers.jsonPath("accessToken").isString)
            .andExpect(MockMvcResultMatchers.jsonPath("refreshToken").isString)
    }

    @Test
    @DisplayName("사용자 서비스와 통신 실패 시 토큰 생성 테스트")
    fun given_invalidUserId_when_createToken_then_expectStatusCode503_and_responseExceptionResponse(){
        Mockito.`when`(userService.findUserId(SOCIAL_TYPE, SOCIAL_USER_NUMBER))
            .thenReturn(-1)

        mvc.createToken()
            .andExpect(MockMvcResultMatchers.status().isServiceUnavailable)
            .expectExceptionResponse()
    }

    @Test
    @DisplayName("리프레시 토큰으로 토큰 재발급 테스트")
    fun given_refreshToken_when_reissueToken_then_reissueAccessToken_and_refreshToken(){
        Mockito.`when`(userService.findUserId(SOCIAL_TYPE, SOCIAL_USER_NUMBER))
            .thenReturn(USER_ID)

        val refreshToken = mvc.createToken().andReturn<TokenResponse>().refreshToken

        assertNotNull(refreshToken)

        mvc.reissueToken(refreshToken)
            .andExpect(MockMvcResultMatchers.status().isCreated)
            .andExpect(MockMvcResultMatchers.jsonPath("accessToken").isString)
    }

    @Test
    @DisplayName("리프레시 토큰 없이 재발급 테스트")
    fun given_emptyRefreshToken_when_reissueToken_then_expectStatusCode400_and_responseExceptionResponse() {
        mvc.reissueToken()
            .andExpect(MockMvcResultMatchers.status().isBadRequest)
            .expectExceptionResponse()
    }


    @Test
    @DisplayName("비정상적인 리프레시 토큰 재발급 테스트")
    fun given_invalidRefreshToken_when_reissueToken_then_expectStatusCode400_and_responseExceptionResponse(){
        mvc.reissueToken("aaa")
            .andExpect(MockMvcResultMatchers.status().isBadRequest)
            .expectExceptionResponse()
    }

    private fun mockOAuthService(){
        Mockito.`when`(oAuthService.oAuth(SOCIAL_TYPE, AUTHORIZATION_CODE))
            .thenReturn(
                OAuth2TokenResponse(
                    accessToken = ACCESS_TOKEN,
                    refreshToken = REFRESH_TOKEN,
                    idToken = ID_TOKEN,
                    expiresIn = 1,
                    tokenType = "t",
                    scope = "",
                    refreshTokenExpiresIn = ""
                )
            )

        Mockito
            .`when`(oAuthService.parseSocialUserNumber(SOCIAL_TYPE, ID_TOKEN))
            .thenReturn(SOCIAL_USER_NUMBER)
    }
}
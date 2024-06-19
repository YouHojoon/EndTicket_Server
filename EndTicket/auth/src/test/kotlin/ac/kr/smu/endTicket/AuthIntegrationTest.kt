package ac.kr.smu.endTicket

import ac.kr.smu.endTicket.auth.domain.model.SocialType
import ac.kr.smu.endTicket.auth.domain.service.OAuthService
import ac.kr.smu.endTicket.auth.domain.service.UserService
import ac.kr.smu.endTicket.auth.infra.oauth2.OAuth2TokenResponse
import ac.kr.smu.endTicket.auth.infra.oauth2.filter.OAuth2AuthorizationFilter
import ac.kr.smu.endTicket.auth.infra.oauth2.filter.OAuth2ErrorHandlerFilter
import ac.kr.smu.endTicket.auth.infra.property.JWTProperties
import ac.kr.smu.endTicket.auth.service.TokenService
import ac.kr.smu.endTicket.auth.ui.controller.AuthController
import ac.kr.smu.endTicket.auth.ui.response.TokenResponse
import ac.kr.smu.endticket.common.redis.config.AutoRedisConfig
import ac.kr.smu.endticket.common.redis.test.RedisTestConfig
import ac.kr.smu.endticket.common.web.test.andReturn
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
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.context.annotation.Import
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.http.MediaType
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.test.web.servlet.setup.DefaultMockMvcBuilder
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext

@SpringBootTest(
    classes = [
        AuthController::class,
        TokenService::class,
        WebMvcAutoConfiguration::class,
        RedisAutoConfiguration::class,
    ]
)
@Import(RedisTestConfig::class,SecurityTestConfig::class, AutoRedisConfig::class)
@EnableConfigurationProperties(JWTProperties::class)
class AuthIntegrationTest @Autowired constructor(
    @MockBean
    private val oAuthService: OAuthService,
    @MockBean
    private val userService: UserService,
    private val ctx: WebApplicationContext,
    private val redisTemplate: RedisTemplate<String, Any>
) {
    private val mvc: MockMvc = MockMvcBuilders
        .webAppContextSetup(ctx)
        .apply<DefaultMockMvcBuilder>(SecurityMockMvcConfigurers.springSecurity())
        .addFilters<DefaultMockMvcBuilder>(OAuth2ErrorHandlerFilter(), OAuth2AuthorizationFilter(oAuthService))
        .build()

    companion object{
        private const val AUTHORIZATION_CODE = "1"
        private const val SOCIAL_USER_NUMBER = "1"
        private const val USER_ID = 1L
        private const val BASE_URL = "http://localhost:8081/auth"
        private const val ACCESS_TOKEN = "ac/kr/smu/endticket/common/redis"
        private const val REFRESH_TOKEN = "r"
        private const val ID_TOKEN = "i"
    }
    private val SOCIAL_TYPE = SocialType.KAKAO


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
    fun given_user_when_createToken_then_return_accessToken_and_refreshToken(){
        Mockito.`when`(userService.findUserID(SOCIAL_TYPE, SOCIAL_USER_NUMBER))
            .thenReturn(USER_ID)

        mvc.perform(
            MockMvcRequestBuilders
                .post("$BASE_URL/sns?socialType=$SOCIAL_TYPE&code=$AUTHORIZATION_CODE")
        )
            .andExpect(MockMvcResultMatchers.jsonPath("accessToken").isString)
            .andExpect(MockMvcResultMatchers.jsonPath("refreshToken").isString)
    }

    @Test
    @DisplayName("리프레시 토큰으로 토큰 재발급 테스트")
    fun given_refreshToken_then_reissueToken_then_reissueAccessToken_and_refreshToken(){
        Mockito.`when`(userService.findUserID(SOCIAL_TYPE, SOCIAL_USER_NUMBER))
            .thenReturn(USER_ID)

        val token = mvc.perform(
            MockMvcRequestBuilders
                .post("$BASE_URL/sns?socialType=$SOCIAL_TYPE&code=$AUTHORIZATION_CODE")
        ).andReturn<TokenResponse>()

        mvc.perform(
            MockMvcRequestBuilders
                .post("$BASE_URL/reissue-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(ObjectMapper().writeValueAsString(mapOf("refreshToken" to token.refreshToken)))
        )
            .andExpect(MockMvcResultMatchers.status().isCreated)
            .andExpect(MockMvcResultMatchers.jsonPath("accessToken").isString)
            .andExpect(MockMvcResultMatchers.jsonPath("refreshToken").isString)
    }

    private fun mockOAuthService(){
        Mockito.`when`(oAuthService.oAuth(SOCIAL_TYPE,AUTHORIZATION_CODE))
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
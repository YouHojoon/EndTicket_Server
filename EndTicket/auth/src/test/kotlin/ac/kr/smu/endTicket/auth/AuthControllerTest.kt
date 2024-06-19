package ac.kr.smu.endTicket.auth

import ac.kr.smu.endTicket.auth.domain.service.OAuthService
import ac.kr.smu.endTicket.auth.infra.oauth2.OAuth2TokenResponse
import ac.kr.smu.endTicket.auth.service.TokenService
import ac.kr.smu.endTicket.auth.service.UserService
import ac.kr.smu.endTicket.auth.ui.controller.AuthController
import ac.kr.smu.endTicket.auth.ui.response.TokenResponse
import ac.kr.smu.endTicket.common.redis.config.AutoRedisConfig
import ac.kr.smu.endTicket.common.redis.test.RedisTestConfig
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.context.annotation.Import
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import kotlin.test.assertNotNull

@WebMvcTest(controllers = [AuthController::class])
@Import(RedisTestConfig::class, SecurityTestConfig::class, AutoRedisConfig::class)
@AutoConfigureMockMvc
class AuthControllerTest @Autowired constructor(
    @MockBean
    private val oAuthService: OAuthService,
    @MockBean
    private val tokenService: TokenService,
    @MockBean
    private val userService: UserService,
    private val mvc: MockMvc
) {

    @BeforeEach
    fun init(){
        mockOAuthService()
        Mockito.`when`(tokenService.createAccessAndRefreshToken(USER_ID))
            .thenReturn(TokenResponse(ACCESS_TOKEN, REFRESH_TOKEN))
    }

    @Test
    @DisplayName("사용자 토큰 생성 테스트")
    fun given_user_when_createToken_then_return_accessToken_and_refreshToken(){
        Mockito.`when`(userService.findUserId(SOCIAL_TYPE, SOCIAL_USER_NUMBER))
            .thenReturn(USER_ID)

        mvc.createToken()
            .andExpect(MockMvcResultMatchers.status().isCreated)
            .andExpect(MockMvcResultMatchers.jsonPath("accessToken").value(ACCESS_TOKEN))
            .andExpect(MockMvcResultMatchers.jsonPath("refreshToken").value(REFRESH_TOKEN))
    }

    @Test
    @DisplayName("리프레시 토큰으로 토큰 재발급 테스트")
    fun given_refreshToken_then_reissueToken_then_reissueAccessToken_and_refreshToken(){
        Mockito.`when`(userService.findUserId(SOCIAL_TYPE, SOCIAL_USER_NUMBER))
            .thenReturn(USER_ID)

        Mockito.`when`(tokenService.reissueToken(REFRESH_TOKEN))
            .thenReturn(TokenResponse(ACCESS_TOKEN, REFRESH_TOKEN))

        val token = tokenService
            .createAccessAndRefreshToken(USER_ID).refreshToken

        assertNotNull(token)

        mvc.reissueToken(token)
            .andExpect(MockMvcResultMatchers.status().isCreated)
            .andExpect(MockMvcResultMatchers.jsonPath("accessToken").value(ACCESS_TOKEN))
            .andExpect(MockMvcResultMatchers.jsonPath("refreshToken").value(REFRESH_TOKEN))
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
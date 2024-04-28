package ac.kr.smu.endTicket

import ac.kr.smu.endTicket.auth.domain.model.SocialType
import ac.kr.smu.endTicket.auth.domain.service.OAuthService
import ac.kr.smu.endTicket.auth.domain.service.UserService
import ac.kr.smu.endTicket.auth.service.TokenService
import ac.kr.smu.endTicket.auth.ui.controller.AuthController
import ac.kr.smu.endTicket.auth.ui.response.CreateTokenResponse
import ac.kr.smu.endTicket.auth.ui.response.ReissueTokenResponse
import ac.kr.smu.endTicket.auth.infra.config.SecurityConfig
import ac.kr.smu.endTicket.auth.infra.OAuth2.OAuth2TokenResponse

import com.fasterxml.jackson.databind.ObjectMapper
import io.jsonwebtoken.UnsupportedJwtException
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers

@WebMvcTest(controllers = [AuthController::class])
@AutoConfigureMockMvc
@Import(SecurityConfig::class)
class AuthControllerTest @Autowired constructor(
    @MockBean
    private val oAuthService: OAuthService,
    @MockBean
    private val tokenService: TokenService,
    @MockBean
    private val userService: UserService,
    private val mvc: MockMvc
) {

    private val AUTHORIZATION_CODE = "1"
    private val SOCIAL_TYPE = SocialType.KAKAO
    private val SOCIAL_UESR_NUMBER = "1"
    private val USER_ID = 1L
    private val BASE_URL = "http://localhost:8081/auth"
    private val ACCESS_TOKEN = "a"
    private val REFRESH_TOKEN = "a"

    @BeforeEach
    fun init(){
        mockOAuthService()
        mockTokenServiceForCreateTokenResponse()
    }

    @Test
    @DisplayName("사용자 토큰 생성 테스트")
    fun given_user_when_createToken_then_return_accessToken_and_refreshToken(){
        Mockito.`when`(userService.findUserID(SOCIAL_TYPE, SOCIAL_UESR_NUMBER))
            .thenReturn(USER_ID)

        mvc.perform(
            MockMvcRequestBuilders
                .post("$BASE_URL/sns?socialType=$SOCIAL_TYPE&code=$AUTHORIZATION_CODE")
        )
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.jsonPath("accessToken").value(ACCESS_TOKEN))
            .andExpect(MockMvcResultMatchers.jsonPath("refreshToken").value(REFRESH_TOKEN))
    }

    @Test
    @DisplayName("리프레시 토큰으로 토큰 재발급 테스트")
    fun given_refreshToken_then_reissueToken_then_reissueAccessToken_and_refreshToken(){
        val token = tokenService.createAccessAndRefreshToken(USER_ID)
        Mockito.`when`(tokenService.reissueToken(Mockito.anyString()))
            .thenReturn(ReissueTokenResponse(ACCESS_TOKEN,REFRESH_TOKEN))

        mvc.perform(
            MockMvcRequestBuilders
                .post("$BASE_URL/reissueToken")
                .contentType(MediaType.APPLICATION_JSON)
                .content(ObjectMapper().writeValueAsString(mapOf("refreshToken" to token.refreshToken)))
        )
            .andExpect(MockMvcResultMatchers.jsonPath("accessToken").value(ACCESS_TOKEN))
            .andExpect(MockMvcResultMatchers.jsonPath("refreshToken").value(REFRESH_TOKEN))
    }

    private fun mockTokenServiceForCreateTokenResponse(){
        Mockito.`when`(tokenService.createAccessAndRefreshToken(USER_ID))
            .thenReturn(CreateTokenResponse(ACCESS_TOKEN,REFRESH_TOKEN))
    }
    private fun mockOAuthService(){
        Mockito.`when`(oAuthService.oAuth(SOCIAL_TYPE,AUTHORIZATION_CODE))
            .thenReturn(
                OAuth2TokenResponse(
                    accessToken = "a",
                    refreshToken = "r",
                    idToken = "i",
                    expiresIn = 1,
                    tokenType = "t",
                    scope = "",
                    refreshTokenExpiresIn = ""
                )
            )

        Mockito
            .`when`(oAuthService.parseSocialUserNumber(SOCIAL_TYPE,"i"))
            .thenReturn(SOCIAL_UESR_NUMBER)
    }
}
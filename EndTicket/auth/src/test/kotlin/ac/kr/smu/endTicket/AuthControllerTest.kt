package ac.kr.smu.endTicket

import ac.kr.smu.endTicket.auth.domain.model.SocialType
import ac.kr.smu.endTicket.auth.domain.service.OAuthService
import ac.kr.smu.endTicket.auth.service.TokenService
import ac.kr.smu.endTicket.auth.ui.controller.AuthController
import ac.kr.smu.endTicket.auth.ui.response.CreateTokenResponse
import ac.kr.smu.endTicket.auth.ui.response.ReissueTokenResponse
import ac.kr.smu.endTicket.infra.config.SecurityConfig
import ac.kr.smu.endTicket.infra.oAuth2.OAuth2TokenResponse
import ac.kr.smu.endTicket.infra.oAuth2.filter.OAuth2AuthorizationFilter
import ac.kr.smu.endTicket.infra.openfeign.CreateUserRequest
import ac.kr.smu.endTicket.infra.openfeign.UserClient
import ac.kr.smu.endTicket.infra.openfeign.UserIDResponse
import com.fasterxml.jackson.databind.ObjectMapper
import feign.Feign
import feign.FeignException
import io.jsonwebtoken.UnsupportedJwtException
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.Mock
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.FilterType
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.security.web.FilterChainProxy
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.test.web.servlet.setup.StandaloneMockMvcBuilder

@WebMvcTest(controllers = [AuthController::class])
@AutoConfigureMockMvc
@Import(SecurityConfig::class)
class AuthControllerTest @Autowired constructor(
    @MockBean
    private val oAuthService: OAuthService,
    @MockBean
    private val tokenService: TokenService,
    @MockBean
    private val userClient: UserClient,
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
        Mockito
            .`when`(userClient.getUserId(SocialType.KAKAO, SOCIAL_UESR_NUMBER))
            .thenReturn(UserIDResponse(USER_ID))


        mvc.perform(
            MockMvcRequestBuilders
                .post("$BASE_URL/sns?socialType=$SOCIAL_TYPE&code=$AUTHORIZATION_CODE")
        )
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.jsonPath("accessToken").value(ACCESS_TOKEN))
            .andExpect(MockMvcResultMatchers.jsonPath("refreshToken").value(REFRESH_TOKEN))
    }

    @Test
    @DisplayName("미가입된 유저 토큰 발급 테스트")
    fun given_notRegisteredUser_when_createToken_then_registerUser_and_return_accessToken_and_refreshToken(){
        val request = Mockito.mock(feign.Request::class.java)
        Mockito
            .`when`(userClient.getUserId(SOCIAL_TYPE, SOCIAL_UESR_NUMBER))
            .thenAnswer {
                throw FeignException.NotFound(
                    "",
                    request,
                    "".toByteArray(),
                    emptyMap()
                )
            }

        val createUserRequest = CreateUserRequest(SOCIAL_UESR_NUMBER,SocialType.KAKAO)
        Mockito.`when`(userClient.createUser(createUserRequest))
            .thenReturn(UserIDResponse(USER_ID))


        mvc.perform(
            MockMvcRequestBuilders
                .post("$BASE_URL/sns?socialType=$SOCIAL_TYPE&code=$AUTHORIZATION_CODE")
        )
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.jsonPath("accessToken").value(ACCESS_TOKEN))
            .andExpect(MockMvcResultMatchers.jsonPath("refreshToken").value(REFRESH_TOKEN))


        Mockito.verify(userClient, Mockito.times(1))
            .createUser(createUserRequest)
    }

    @Test
    @DisplayName("토큰 검증 테스트")
    fun given_accessToken_when_validationToken_then_responseStatus204(){
        val token = tokenService.createAccessAndRefreshToken(USER_ID)

        mvc.perform(
            MockMvcRequestBuilders
                .post("$BASE_URL/validation")
                .header("Authorization", "Barer ${token.accessToken}")

        ).andExpect(MockMvcResultMatchers.status().isNoContent)
    }

    @Test
    @DisplayName("토큰 없을 시 접근 금지 테스트")
    fun notGiven_accessToken_when_validationToken_then_responseStatus401(){
        mvc.perform(
            MockMvcRequestBuilders
                .post("$BASE_URL/validation")
        )
            .andExpect(MockMvcResultMatchers.status().isUnauthorized)
    }

    @Test
    @DisplayName("리프레시 토큰으로 토큰 검증")
    fun given_refreshToken_when_validationToken_thenResponseStatus400(){
        val token = tokenService.createAccessAndRefreshToken(USER_ID)
        Mockito.`when`(tokenService.parseUserID(token.refreshToken))
            .thenAnswer {
                throw UnsupportedJwtException("")
            }

        mvc.perform(
            MockMvcRequestBuilders
                .post("$BASE_URL/validation")
                .header("Authorization", "Barer ${token.refreshToken}")
        ).andExpect(MockMvcResultMatchers.status().isBadRequest)

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
package ac.kr.smu.endticket.auth

import ac.kr.smu.endTicket.auth.domain.service.OAuthService
import ac.kr.smu.endTicket.auth.service.TokenService
import ac.kr.smu.endTicket.auth.service.UserService
import ac.kr.smu.endTicket.auth.ui.controller.AuthController
import ac.kr.smu.endTicket.auth.ui.response.TokenResponse
import ac.kr.smu.endticket.common.redis.config.AutoRedisConfig
import ac.kr.smu.endticket.common.redis.test.RedisTestConfig
import ac.kr.smu.endticket.common.web.test.expectExceptionResponse
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.context.annotation.Import
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import java.lang.IllegalStateException
import java.util.concurrent.CompletableFuture

@WebMvcTest(controllers = [AuthController::class])
@Import(RedisTestConfig::class, SecurityTestConfig::class, AutoRedisConfig::class)
@AutoConfigureMockMvc
class AuthControllerTest
    @Autowired
    constructor(
        @MockBean
        private val oauthService: OAuthService,
        @MockBean
        private val tokenService: TokenService,
        @MockBean
        private val userService: UserService,
        private val mvc: MockMvc,
    ) {
        @BeforeEach
        fun init() {
            mockOauthService(oauthService)
            Mockito
                .`when`(tokenService.createAccessAndRefreshToken(AuthTestParameters.USER_ID))
                .thenReturn(TokenResponse(AuthTestParameters.ACCESS_TOKEN, AuthTestParameters.REFRESH_TOKEN))
        }

        @Test
        @DisplayName("사용자 토큰 생성 테스트")
        fun given_user_when_createToken_then_responseAccessTokenAndRefreshToken() {
            Mockito
                .`when`(userService.findUserId(AuthTestParameters.SOCIAL_TYPE, AuthTestParameters.SOCIAL_USER_NUMBER))
                .thenReturn(CompletableFuture.completedFuture(AuthTestParameters.USER_ID))

            mvc
                .createToken()
                .andExpect(MockMvcResultMatchers.status().isCreated)
                .andExpect(MockMvcResultMatchers.jsonPath("accessToken").value(AuthTestParameters.ACCESS_TOKEN))
                .andExpect(MockMvcResultMatchers.jsonPath("refreshToken").value(AuthTestParameters.REFRESH_TOKEN))
        }

        @Test
        @DisplayName("사용자 서비스와 통신 실패 시 토큰 생성 테스트")
        fun given_invalidUserId_when_createToken_then_responseExceptionResponseWithStatus503() {
            Mockito
                .`when`(userService.findUserId(AuthTestParameters.SOCIAL_TYPE, AuthTestParameters.SOCIAL_USER_NUMBER))
                .thenReturn(CompletableFuture.failedFuture(RuntimeException()))

            mvc
                .createToken()
                .andExpect(MockMvcResultMatchers.status().isServiceUnavailable)
                .expectExceptionResponse()
        }

        @Test
        @DisplayName("리프레시 토큰으로 토큰 재발급 테스트")
        fun given_refreshToken_when_reissueToken_then_reissueAccessTokenAndRefreshToken() {
            Mockito
                .`when`(tokenService.reissueToken(AuthTestParameters.REFRESH_TOKEN))
                .thenReturn(TokenResponse(AuthTestParameters.ACCESS_TOKEN, AuthTestParameters.REFRESH_TOKEN))

            mvc
                .reissueToken(AuthTestParameters.REFRESH_TOKEN)
                .andExpect(MockMvcResultMatchers.status().isCreated)
                .andExpect(MockMvcResultMatchers.jsonPath("accessToken").value(AuthTestParameters.ACCESS_TOKEN))
                .andExpect(MockMvcResultMatchers.jsonPath("refreshToken").value(AuthTestParameters.REFRESH_TOKEN))
        }

        @ParameterizedTest
        @DisplayName("비정상적인 리프레시 토큰 재발급 테스트")
        @MethodSource("${AuthTestParameters.PATH}#provideInvalidRefreshToken")
        fun given_invalidRefreshToken_when_reissueToken_then_responseExceptionResponseWithStatus400(token: String?) {
            Mockito
                .`when`(tokenService.reissueToken(Mockito.anyString()))
                .thenThrow(IllegalArgumentException(""))

            mvc
                .reissueToken(token)
                .andExpect(MockMvcResultMatchers.status().isBadRequest)
                .expectExceptionResponse()
        }
    }

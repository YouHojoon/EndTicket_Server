//package ac.kr.smu.endticket.auth
//
//import ac.kr.smu.endTicket.auth.domain.exception.UserExpiredException
//import ac.kr.smu.endTicket.auth.domain.service.OAuth2Service
//import ac.kr.smu.endTicket.auth.service.TokenService
//import ac.kr.smu.endTicket.auth.service.UserService
//import ac.kr.smu.endTicket.auth.ui.controller.AuthController
//import ac.kr.smu.endTicket.auth.ui.response.TokenResponse
//import ac.kr.smu.endticket.common.redis.config.AutoRedisConfig
//import ac.kr.smu.endticket.common.redis.test.RedisTestConfig
//import ac.kr.smu.endticket.common.web.test.expectExceptionResponse
//import org.junit.jupiter.api.BeforeEach
//import org.junit.jupiter.api.DisplayName
//import org.junit.jupiter.api.Nested
//import org.junit.jupiter.api.Test
//import org.junit.jupiter.params.ParameterizedTest
//import org.junit.jupiter.params.provider.MethodSource
//import org.mockito.Mockito
//import org.springframework.beans.factory.annotation.Autowired
//import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
//import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
//import org.springframework.boot.test.mock.mockito.MockBean
//import org.springframework.context.annotation.Import
//import org.springframework.test.web.servlet.MockMvc
//import org.springframework.test.web.servlet.result.MockMvcResultMatchers
//import java.util.concurrent.CompletableFuture
//import kotlin.test.BeforeTest
//
//@WebMvcTest(controllers = [AuthController::class])
//@Import(RedisTestConfig::class, SecurityTestConfig::class, AutoRedisConfig::class)
//@AutoConfigureMockMvc
//class AuthControllerTest
//    @Autowired
//    constructor(
//        @MockBean
//        private val oauth2Service: OAuth2Service,
//        @MockBean
//        private val tokenService: TokenService,
//        @MockBean
//        private val userService: UserService,
//        private val mvc: MockMvc,
//    ) {
//        @BeforeEach
//        fun init() {
//            mockOAuth2Service(oauth2Service)
//            Mockito
//                .`when`(tokenService.createAccessAndRefreshToken(AuthTestParameters.USER_ID))
//                .thenReturn(TokenResponse(AuthTestParameters.ACCESS_TOKEN, AuthTestParameters.REFRESH_TOKEN))
//        }
//
//        @Nested
//        @DisplayName("토큰 발급 시")
//        inner class Describe_createToken {
//            @BeforeTest
//            fun init(){
//                Mockito
//                    .`when`(
//                        userService.findUserId(
//                            AuthTestParameters.SOCIAL_TYPE,
//                            AuthTestParameters.SOCIAL_USER_NUMBER,
//                        ),
//                    ).thenReturn(CompletableFuture.completedFuture(AuthTestParameters.USER_ID))
//
//            }
//
//            @Nested
//            @DisplayName("SNS 종류와 인증 코드로 요청하면")
//            inner class Context_with_socialType_authorizationCode {
//
//                @Test
//                @DisplayName("")
//                fun it_response_accessToken_refreshToken() {
//                    mvc
//                        .createToken()
//                        .andExpect(MockMvcResultMatchers.status().isCreated)
//                        .andExpect(MockMvcResultMatchers.jsonPath("accessToken").value(AuthTestParameters.ACCESS_TOKEN))
//                        .andExpect(
//                            MockMvcResultMatchers.jsonPath("refreshToken").value(AuthTestParameters.REFRESH_TOKEN)
//                        )
//                }
//        }
//
//
//
//            @Test
//            @DisplayName("SNS 종류와 인증 코드로 요청하면 access 토큰과 refresh 토큰을 반환한다.")
//            fun given_socialTypeAndAuthorizationCode_then_responseAccessTokenAndRefreshToken() {
//
//            @Test
//            @DisplayName("사용자 서비스와 통신 실패하면 503 에러를 반환한다.")
//            fun given_invalidUserId_then_responseExceptionResponseWithStatus503() {
//                Mockito
//                    .`when`(
//                        userService.findUserId(
//                            AuthTestParameters.SOCIAL_TYPE,
//                            AuthTestParameters.SOCIAL_USER_NUMBER,
//                        ),
//                    ).thenReturn(CompletableFuture.failedFuture(RuntimeException()))
//
//                mvc
//                    .createToken()
//                    .andExpect(MockMvcResultMatchers.status().isServiceUnavailable)
//                    .expectExceptionResponse()
//            }
//        }
//
//        @Nested
//        @DisplayName("토큰 갱신 시")
//        inner class ReissueToken {
//            @Test
//            @DisplayName("리프레시 토큰으로 요청하면 access 토큰 , refresh 토큰을 갱신한다.")
//            fun given_refreshToken_then_reissueAccessTokenAndRefreshToken() {
//                Mockito
//                    .`when`(tokenService.reissueToken(AuthTestParameters.REFRESH_TOKEN))
//                    .thenReturn(TokenResponse(AuthTestParameters.ACCESS_TOKEN, AuthTestParameters.REFRESH_TOKEN))
//
//                mvc
//                    .reissueToken(AuthTestParameters.REFRESH_TOKEN)
//                    .andExpect(MockMvcResultMatchers.status().isCreated)
//                    .andExpect(MockMvcResultMatchers.jsonPath("accessToken").value(AuthTestParameters.ACCESS_TOKEN))
//                    .andExpect(MockMvcResultMatchers.jsonPath("refreshToken").value(AuthTestParameters.REFRESH_TOKEN))
//            }
//
//            @ParameterizedTest
//            @DisplayName("비정상적인 리프레시 토큰이면 400 에러를 반환한다.")
//            @MethodSource("${AuthTestParameters.PATH}#provideInvalidRefreshToken")
//            fun given_invalidRefreshToken_when_reissueToken_then_responseExceptionResponseWithStatus400(token: String?) {
//                Mockito
//                    .`when`(tokenService.reissueToken(Mockito.anyString()))
//                    .thenThrow(IllegalArgumentException(""))
//
//                mvc
//                    .reissueToken(token)
//                    .andExpect(MockMvcResultMatchers.status().isBadRequest)
//                    .expectExceptionResponse()
//            }
//
//            @Test
//            @DisplayName("만료된 사용자면 401 에러를 반환한다.")
//            fun given_expiredUser_then_responseExceptionResponseWithStatus401() {
//                Mockito
//                    .`when`(tokenService.reissueToken(AuthTestParameters.REFRESH_TOKEN))
//                    .thenThrow(UserExpiredException(AuthTestParameters.USER_ID))
//
//                mvc
//                    .reissueToken(AuthTestParameters.REFRESH_TOKEN)
//                    .andExpect(MockMvcResultMatchers.status().isUnauthorized)
//                    .expectExceptionResponse()
//            }
//        }
//    }

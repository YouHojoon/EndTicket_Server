package ac.kr.smu.endticket.auth

import ac.kr.smu.endticket.auth.domain.exception.UserExpiredException
import ac.kr.smu.endticket.auth.service.TokenService
import ac.kr.smu.endticket.auth.service.UserService
import ac.kr.smu.endticket.auth.ui.controller.AuthController
import ac.kr.smu.endticket.common.redis.config.AutoRedisConfig
import ac.kr.smu.endticket.common.redis.test.RedisTestConfig
import ac.kr.smu.endticket.common.web.test.expectExceptionResponse
import com.ninjasquad.springmockk.MockkBean
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.core.spec.style.scopes.DescribeSpecContainerScope
import io.kotest.extensions.spring.SpringExtension
import io.mockk.every
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.http.HttpStatus
import org.springframework.test.web.servlet.MockMvc
import java.util.concurrent.CompletableFuture

@WebMvcTest(controllers = [AuthController::class])
@Import(RedisTestConfig::class, SecurityTestConfig::class, AutoRedisConfig::class)
@AutoConfigureMockMvc
class AuthControllerTest : DescribeSpec() {
    override fun extensions() = listOf(SpringExtension)

    @Autowired
    lateinit var mvc: MockMvc

    @MockkBean
    private lateinit var tokenService: TokenService

    @MockkBean
    private lateinit var userService: UserService

    init {
        describe("토큰 발급 시") {
            context("SNS 종류와 인증 코드로 요청하는 경우") {
                context("사용자 서버와 통신을 성공했을 때") {
                    beforeContainer {
                        every {
                            userService.findUserId(
                                AuthTestParameters.SOCIAL_TYPE,
                                AuthTestParameters.SOCIAL_USER_NUMBER,
                            )
                        } returns CompletableFuture.completedFuture(AuthTestParameters.USER_ID)
                    }
                    context("만료된 사용자라면") {
                        every { tokenService.createAccessAndRefreshToken(AuthTestParameters.USER_ID) } throws
                            UserExpiredException()

                        it("401 에러를 반환한다.") {
                            mvc
                                .createToken()
                                .expectExceptionResponse(HttpStatus.UNAUTHORIZED)
                        }
                    }

                    context("정상적인 사용자라면") {
                        every { tokenService.createAccessAndRefreshToken(AuthTestParameters.USER_ID) } returns
                            AuthTestParameters.TOKEN_RESPONSE
                        it("access 토큰과 refresh 토큰을 반환한다.") {
                            mvc
                                .createToken()
                                .andExpect {
                                    status { isOk() }
                                    jsonPath("accessToken") {
                                        value(AuthTestParameters.ACCESS_TOKEN)
                                    }
                                    jsonPath("refreshToken") {
                                        value(AuthTestParameters.REFRESH_TOKEN)
                                    }
                                }
                        }
                    }
                }

                context("사용자 서비스와 통신에 실패하면") {
                    every {
                        userService.findUserId(
                            AuthTestParameters.SOCIAL_TYPE,
                            AuthTestParameters.SOCIAL_USER_NUMBER,
                        )
                    } returns CompletableFuture.failedFuture(RuntimeException())

                    it("503 에러를 반환한다") {
                        mvc
                            .createToken()
                            .expectExceptionResponse(HttpStatus.SERVICE_UNAVAILABLE)
                    }
                }
            }
        }

        describe("토큰 갱신 시") {
            suspend fun DescribeSpecContainerScope.it_response_badRequest(refreshToken: String? = null) =
                it("400에러를 반환한다.") {
                    mvc
                        .reissueToken(refreshToken)
                        .expectExceptionResponse(HttpStatus.BAD_REQUEST)
                }

            context("정상적인 refresh 토큰으로 요청하는 경우") {
                context("정상적인 사용자라면") {
                    every { tokenService.reissueToken(AuthTestParameters.REFRESH_TOKEN) } returns AuthTestParameters.TOKEN_RESPONSE
                    it("access 토큰, refresh 토큰을 갱신한다.") {
                        mvc
                            .reissueToken()
                            .andExpect {
                                status { isOk() }
                                jsonPath("accessToken") {
                                    value(AuthTestParameters.ACCESS_TOKEN)
                                }
                                jsonPath("refreshToken") {
                                    value(AuthTestParameters.REFRESH_TOKEN)
                                }
                            }
                    }
                }
                context("만료된 사용자라면") {
                    every { tokenService.reissueToken(AuthTestParameters.REFRESH_TOKEN) } throws
                        UserExpiredException()
                    it("401에러를 반환한다.") {
                        mvc.reissueToken()
                    }
                }
            }

            context("refresh 토큰이 없으면") {
                it_response_badRequest()
            }

            context("비정상적인 refresh 토큰이면") {
                val invalidRefreshToken = "invalid refresh token"
                every { tokenService.reissueToken(invalidRefreshToken) } throws IllegalArgumentException("aa")
                it_response_badRequest(invalidRefreshToken)
            }
        }
    }
}

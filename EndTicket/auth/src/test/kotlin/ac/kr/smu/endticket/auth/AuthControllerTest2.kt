package ac.kr.smu.endticket.auth

import ac.kr.smu.endTicket.auth.domain.service.OAuth2Service
import ac.kr.smu.endTicket.auth.infra.oauth2.OAuth2TokenResponse
import ac.kr.smu.endTicket.auth.service.TokenService
import ac.kr.smu.endTicket.auth.service.UserService
import ac.kr.smu.endTicket.auth.ui.controller.AuthController
import ac.kr.smu.endTicket.auth.ui.response.TokenResponse
import ac.kr.smu.endticket.common.redis.config.AutoRedisConfig
import ac.kr.smu.endticket.common.redis.test.RedisTestConfig
import com.ninjasquad.springmockk.MockkBean
import io.kotest.core.spec.Spec
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.core.test.TestCase
import io.kotest.extensions.spring.SpringExtension
import io.mockk.every
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.test.web.servlet.MockMvc
import java.util.concurrent.CompletableFuture

@WebMvcTest(controllers = [AuthController::class])
@Import(RedisTestConfig::class, SecurityTestConfig::class, AutoRedisConfig::class)
@AutoConfigureMockMvc
class AuthControllerTest2 : DescribeSpec() {
    override fun extensions() = listOf(SpringExtension)

    @Autowired
    lateinit var mvc: MockMvc

    @MockkBean
    private lateinit var oauth2Service: OAuth2Service

    @MockkBean
    private lateinit var tokenService: TokenService

    @MockkBean
    private lateinit var userService: UserService

    override suspend fun beforeTest(testCase: TestCase) {
        every {
            oauth2Service.oAuth(
                AuthTestParameters.SOCIAL_TYPE,
                AuthTestParameters.AUTHORIZATION_CODE,
            )
        } returns
            OAuth2TokenResponse(
                accessToken = AuthTestParameters.ACCESS_TOKEN,
                refreshToken = AuthTestParameters.REFRESH_TOKEN,
                idToken = AuthTestParameters.ID_TOKEN,
                expiresIn = 1,
                tokenType = "t",
                scope = "",
                refreshTokenExpiresIn = "",
            )

        every {
            oauth2Service.parseSocialUserNumber(
                AuthTestParameters.SOCIAL_TYPE,
                AuthTestParameters.ID_TOKEN,
            )
        } returns AuthTestParameters.SOCIAL_USER_NUMBER

        every { tokenService.createAccessAndRefreshToken(AuthTestParameters.USER_ID) } returns
            TokenResponse(
                AuthTestParameters.ACCESS_TOKEN,
                AuthTestParameters.REFRESH_TOKEN,
            )
    }

    init {
        describe("토큰 발급 시") {
            context("SNS 종류와 인증 코드로 요청하면") {
                every {
                    userService.findUserId(
                        AuthTestParameters.SOCIAL_TYPE,
                        AuthTestParameters.SOCIAL_USER_NUMBER,
                    )
                } returns CompletableFuture.completedFuture(AuthTestParameters.USER_ID)

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

            context("사용자 서비스와 통신에 실패하면"){
                it("503 에러를 반환한다"){
                    mvc.createToken()
                        .andExpect {
                            status { isServiceUnavailable() }
                        }
                }
            }

        }
    }
}

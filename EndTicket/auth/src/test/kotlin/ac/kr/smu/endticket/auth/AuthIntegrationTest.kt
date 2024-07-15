package ac.kr.smu.endticket.auth

import ac.kr.smu.endticket.auth.config.property.JWTProperties
import ac.kr.smu.endticket.auth.service.TokenService
import ac.kr.smu.endticket.auth.service.UserService
import ac.kr.smu.endticket.auth.ui.controller.AuthController
import ac.kr.smu.endticket.auth.ui.response.TokenResponse
import ac.kr.smu.endticket.common.redis.config.AutoRedisConfig
import ac.kr.smu.endticket.common.redis.test.RedisTestConfig
import ac.kr.smu.endticket.common.web.test.andReturn
import ac.kr.smu.endticket.common.web.test.expectExceptionResponse
import com.ninjasquad.springmockk.SpykBean
import io.jsonwebtoken.Jwts
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.core.spec.style.scopes.DescribeSpecContainerScope
import io.kotest.extensions.spring.SpringExtension
import io.mockk.every
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.http.HttpStatus
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.web.servlet.MockMvc
import java.util.concurrent.CompletableFuture

@SpringBootTest(
    classes = [
        AuthController::class,
        TokenService::class,
        GrpcConfig::class,
        UserService::class,
        RedisAutoConfiguration::class,
        WebMvcAutoConfiguration::class,
    ],
)
@Import(RedisTestConfig::class, SecurityTestConfig::class, AutoRedisConfig::class)
@AutoConfigureMockMvc
@EnableConfigurationProperties(JWTProperties::class)
@DirtiesContext
class AuthIntegrationTest : DescribeSpec() {
    override fun extensions() = listOf(SpringExtension)

    @SpykBean
    private lateinit var userService: UserService

    @Autowired
    lateinit var mvc: MockMvc

    @Autowired
    private lateinit var tokenService: TokenService

    @Autowired
    private lateinit var redisTemplate: RedisTemplate<String, Any>

    @Autowired
    private lateinit var jwtProperties: JWTProperties

    init {
        afterContainer {
            redisTemplate.connectionFactory
                ?.connection
                ?.serverCommands()
                ?.flushAll()
        }

        describe("토큰 발급 시") {
            context("SNS 종류와 인증 코드로 요청하는 경우") {
                context("사용자 서버와 통신을 성공했을 때") {
                    context("만료된 사용자라면") {
                        tokenService.expireAccessAndRefreshToken(AuthTestParameters.USER_ID)
                        it("401 에러를 반환한다.") {
                            mvc
                                .createToken()
                                .expectExceptionResponse(HttpStatus.UNAUTHORIZED)
                        }
                    }
                    context("정상적인 사용자라면") {
                        it("access 토큰과 refresh 토큰을 반환한다.") {
                            mvc
                                .createToken()
                                .andExpect {
                                    status { isOk() }
                                    jsonPath("accessToken") {
                                        isString()
                                    }
                                    jsonPath("refreshToken") {
                                        isString()
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

            context("만료 기한이 충분한 refresh 토큰으로 요청하는 경우") {
                context("정상적인 사용자라면") {
                    val refreshToken = mvc.createToken().andReturn<TokenResponse>().refreshToken

                    it("access 토큰을 갱신한다.") {
                        mvc
                            .reissueToken(refreshToken)
                            .andExpect {
                                status { isOk() }
                                jsonPath("accessToken") {
                                    isString()
                                }
                            }
                    }
                }
                context("만료된 사용자라면") {
                    tokenService.expireAccessAndRefreshToken(AuthTestParameters.USER_ID)
                    it("401에러를 반환한다.") {
                        mvc.reissueToken()
                    }
                }
            }

            context("만료기한이 임박한 refresh 토큰으로 요청하는 경우") {
                val refreshToken = Jwts.builder().createMockRefreshToken(jwtProperties.secret, 1000 * 10)
                redisTemplate.opsForValue().set(refreshToken, AuthTestParameters.USER_ID)
                it("access 토큰과 refresh 토큰을 함께 발급받는다.") {
                    mvc
                        .reissueToken(refreshToken)
                        .andExpect {
                            status { isOk() }
                            jsonPath("accessToken") {
                                isString()
                            }
                            jsonPath("refreshToken") {
                                isString()
                            }
                        }
                }
            }

            context("refresh 토큰이 없으면") {
                it_response_badRequest()
            }

            context("비정상적인 refresh 토큰이면") {
                val invalidRefreshToken = "invalid refresh token"
                it_response_badRequest(invalidRefreshToken)
            }
        }
    }
}

package ac.kr.smu.endticket.auth

import ac.kr.smu.endTicket.auth.config.property.JWTProperties
import ac.kr.smu.endTicket.auth.constant.RedisConstant
import ac.kr.smu.endTicket.auth.domain.exception.RefreshTokenExpiredException
import ac.kr.smu.endTicket.auth.domain.exception.UserExpiredException
import ac.kr.smu.endTicket.auth.service.TokenService
import ac.kr.smu.endticket.common.redis.test.RedisTestConfig
import ac.kr.smu.endticket.protobuf.AccessToken
import ac.kr.smu.endticket.protobuf.TokenServiceGrpc.TokenServiceBlockingStub
import io.jsonwebtoken.Jwts
import net.devh.boot.grpc.client.inject.GrpcClient
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.context.ConfigurationPropertiesAutoConfiguration
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.context.annotation.Import
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.core.SetOperations
import org.springframework.data.redis.core.ValueOperations
import org.springframework.test.annotation.DirtiesContext
import kotlin.test.BeforeTest
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

@SpringBootTest(
    classes = [
        TokenService::class,
        GrpcConfig::class,
        RedisAutoConfiguration::class
    ],
)
@Import(RedisTestConfig::class)
@EnableConfigurationProperties(JWTProperties::class)
@DirtiesContext
class TokenServiceTest
    @Autowired
    constructor(
        @MockBean
        private val valueOperations: ValueOperations<String, String>,
        @MockBean
        private val setOperations: SetOperations<String, String>,
        @MockBean
        private val redisTemplate: RedisTemplate<String, String>,
        private val service: TokenService,
        private val properties: JWTProperties,
    ) {
        @GrpcClient("token")
        private lateinit var stub: TokenServiceBlockingStub

        @BeforeTest
        fun init() {
            Mockito.`when`(redisTemplate.opsForSet()).thenReturn(setOperations)
            Mockito.`when`(redisTemplate.opsForValue()).thenReturn(valueOperations)
        }

        @Test
        @DisplayName("Access 토큰 검증 테스트")
        @DirtiesContext
        fun given_accessToken_when_validateToken_then_returnResponse() {
            val token = service.createAccessAndRefreshToken(AuthTestParameters.USER_ID).accessToken
            val response =
                stub.validateAccessToken(
                    AccessToken.newBuilder().setToken(token).build(),
                )

            assertEquals(AuthTestParameters.USER_ID, response.userId)
            assertEquals(200, response.status)
        }

        @ParameterizedTest
        @DisplayName("올바르지 않은 Access 토큰 검증 테스트")
        @MethodSource("${AuthTestParameters.PATH}#provideInvalidAccessTokenAndExpectedStatus")
        @DirtiesContext
        fun given_invalidAccessToken_when_validateToken_then_returnResponseWithExpectedStatus(
            accessToken: AccessToken,
            expectedStatus: Int,
        ) {
            val response = stub.validateAccessToken(accessToken)

            assertEquals(expectedStatus, response.status)
            assertEquals(-1, response.userId)
        }

        @Test
        @DisplayName("리프레시 토큰으로 토큰 검증 테스트")
        @DirtiesContext
        fun given_refreshToken_when_validateToken_then_returnResponseWithStatus400() {
            val createTokenResponse = service.createAccessAndRefreshToken(AuthTestParameters.USER_ID)
            val response =
                stub.validateAccessToken(
                    AccessToken
                        .newBuilder()
                        .setToken(createTokenResponse.refreshToken)
                        .build(),
                )

            assertEquals(response.userId, -1)
            assertEquals(response.status, 400)
        }

        @Test
        @DisplayName("정상 유저 토큰 발급 테스트")
        fun given_userID_when_createAccessAndRefreshToken_then_success() {
            assertDoesNotThrow {
                service.createAccessAndRefreshToken(AuthTestParameters.USER_ID)
            }
        }

        @Test
        @DisplayName("access 토큰 재발급 테스트")
        fun given_refreshToken_when_reissueToken_then_returnOnlyAccessToken() {
            val token = service.createAccessAndRefreshToken(AuthTestParameters.USER_ID)
            val refreshToken = token.refreshToken

            assertNotNull(refreshToken)

            Mockito
                .`when`(valueOperations.get(refreshToken))
                .thenReturn(AuthTestParameters.USER_ID.toString())

            assertDoesNotThrow {
                assertNull(service.reissueToken(refreshToken).refreshToken)
            }
        }

        @Test
        @DisplayName("만료가 임박한 refresh 토큰 재발급 테스트")
        fun given_refreshTokenReachedReissueExpiration_when_reissueToken_then_success() {
            val refreshToken = Jwts.builder().createMockRefreshToken(properties.secret, 1000 * 10)

            assertNotNull(refreshToken)

            Mockito
                .`when`(valueOperations.get(refreshToken))
                .thenReturn(AuthTestParameters.USER_ID.toString())

            val response = service.reissueToken(refreshToken)
            assertNotNull(response.refreshToken)
        }

        @Test
        @DisplayName("만료된 refresh 토큰 재발급 테스트")
        fun given_expiredRefreshToken_when_reissueToken_then_success() {
            val refreshToken = Jwts.builder().createMockRefreshToken(properties.secret, 0)

            assertNotNull(refreshToken)

            Mockito
                .`when`(valueOperations.get(refreshToken))
                .thenReturn(AuthTestParameters.USER_ID.toString())

            assertThrows<RefreshTokenExpiredException> { service.reissueToken(refreshToken) }
        }

        @Test
        @DisplayName("캐시에 저장되어 있지 않은 refresh 토큰으로 access 토큰 재발급 테스트")
        fun given_notStoredRefreshToken_when_reissueToken_then_throwIllegalArgumentException() {
            val token = service.createAccessAndRefreshToken(AuthTestParameters.USER_ID)
            val refreshToken = token.refreshToken

            assertNotNull(refreshToken)
            assertThrows<IllegalArgumentException> {
                service.reissueToken(refreshToken)
            }
        }

        @Test
        @DisplayName("사용자 만료 테스트")
        fun given_userId_when_expireAccessTokenAndRefreshToken_then_saveUserIdAtRedis() {
            service.expireAccessAndRefreshToken(AuthTestParameters.USER_ID)

            Mockito.verify(setOperations).add(RedisConstant.EXPIRED_USERS_REDIS_KEY, AuthTestParameters.USER_ID.toString())
        }

        @Test
        @DisplayName("만료된 사용자 access 토큰 발급 테스트")
        fun given_expiredUser_when_createAccessTokenAndRefreshToken_then_throwUserExpiredException() {
            Mockito
                .`when`(
                    setOperations.isMember(
                        RedisConstant.EXPIRED_USERS_REDIS_KEY,
                        AuthTestParameters.USER_ID.toString(),
                    ),
                ).thenReturn(true)

            assertThrows<UserExpiredException> { service.createAccessAndRefreshToken(AuthTestParameters.USER_ID) }
        }

        @Test
        @DisplayName("만료된 사용자 access 토큰 발급 테스트")
        fun given_accessTokenOfExpiredUser_when_validateToken_then_returnResponseWithStatus401() {
            val accessToken = service.createAccessAndRefreshToken(AuthTestParameters.USER_ID).accessToken

            Mockito
                .`when`(
                    setOperations.isMember(
                        RedisConstant.EXPIRED_USERS_REDIS_KEY,
                        AuthTestParameters.USER_ID.toString(),
                    ),
                ).thenReturn(true)

            val response = stub.validateAccessToken(AccessToken.newBuilder().setToken(accessToken).build())

            assertEquals(401,response.status)
        }

        @Test
        @DisplayName("만료된 사용자 토큰 갱신 테스트")
        fun given_expiredUser_when_reissueToken_then_throwUserExpiredException(){
            val refreshToken = service.createAccessAndRefreshToken(AuthTestParameters.USER_ID).refreshToken!!

            Mockito
                .`when`(
                    setOperations.isMember(
                        RedisConstant.EXPIRED_USERS_REDIS_KEY,
                        AuthTestParameters.USER_ID.toString(),
                    ),
                ).thenReturn(true)
            Mockito.`when`(valueOperations.get(refreshToken))
                .thenReturn(AuthTestParameters.USER_ID.toString())

            assertThrows<UserExpiredException> { service.reissueToken(refreshToken)  }
            Mockito.verify(redisTemplate).delete(refreshToken)
        }
    }

package ac.kr.smu.endticket.auth

import ac.kr.smu.endTicket.auth.domain.exception.RefreshTokenExpiredException
import ac.kr.smu.endTicket.auth.infra.property.JWTProperties
import ac.kr.smu.endTicket.auth.service.TokenService
import ac.kr.smu.endticket.common.redis.test.RedisTestConfig
import ac.kr.smu.endticket.protobuf.AccessToken
import ac.kr.smu.endticket.protobuf.TokenServiceGrpc.TokenServiceBlockingStub
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.UnsupportedJwtException
import net.devh.boot.grpc.client.inject.GrpcClient
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.context.annotation.Import
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.core.ValueOperations
import org.springframework.test.annotation.DirtiesContext
import kotlin.test.BeforeTest
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

@SpringBootTest(classes = [
    TokenService::class,
    GrpcConfig::class
])
@Import(RedisTestConfig::class)
@EnableConfigurationProperties(JWTProperties::class)
@DirtiesContext
class TokenServiceTest @Autowired constructor(
    @MockBean
    private val ops: ValueOperations<String,String>,
    @MockBean
    private val redisTemplate: RedisTemplate<String,String>,
    private val service: TokenService,
    private val properties:JWTProperties,

){
    @GrpcClient("token")
    private lateinit var stub: TokenServiceBlockingStub

    @BeforeTest
    fun init(){
        Mockito.`when`(redisTemplate.opsForValue()).thenReturn(ops)
    }

    @Test
    @DisplayName("Access 토큰 검증 테스트")
    @DirtiesContext
    fun given_accessToken_when_validationToken_then_returnResponse(){
        val token = service.createAccessAndRefreshToken(USER_ID).accessToken
        val response = stub.validateAccessToken(
            AccessToken.newBuilder().setToken(token).build()
        )

        assertEquals(USER_ID, response.userId)
        assertEquals(200, response.status)
    }


    @ParameterizedTest
    @DisplayName("올바르지 않은 Access 토큰 검증 테스트")
    @MethodSource("ac.kr.smu.endticket.auth.AuthTestParameters#provideInvalidAccessTokenAndExpectedStatus")
    @DirtiesContext
    fun given_invalidAccessToken_when_validationToken_then_returnResponseWithExpepectedStatus(accessToken: AccessToken, expectedStatus: Int){
        val response = stub.validateAccessToken(accessToken)

        assertEquals(expectedStatus, response.status)
        assertEquals(-1,response.userId)
    }

    @Test
    @DisplayName("리프레시 토큰으로 토큰 검증 테스트")
    @DirtiesContext
    fun given_refreshToken_when_validationToken_then_returnResponseStatus400(){
        val createTokenResponse = service.createAccessAndRefreshToken(USER_ID)
        val response = stub.validateAccessToken(
            AccessToken.newBuilder()
                .setToken(createTokenResponse.refreshToken)
                .build()
        )

        assertEquals(response.userId, -1)
        assertEquals(response.status, 400)
    }
    @Test
    @DisplayName("정상 유저 토큰 발급 테스트")
    fun given_userID_when_createAccessAndRefreshToken_then_success(){
        assertDoesNotThrow {
            service.createAccessAndRefreshToken(userId = USER_ID)
        }
    }

    @Test
    @DisplayName("access 토큰으로 사용자 ID 파싱")
    fun given_accessToken_when_parseUserID_then_returnUserID() {
        val token = service.createAccessAndRefreshToken(USER_ID)

        assertEquals(service.parseUserId(token.accessToken), USER_ID)
    }

    @Test
    @DisplayName("refresh 토큰으로 사용자 ID 파싱 테스트")
    fun given_refreshToken_when_parseUserID_then_throwUnsupportedJwtException(){
        val token = service.createAccessAndRefreshToken(USER_ID)
        val refreshToken = token.refreshToken

        assertNotNull(refreshToken)
        assertThrows<UnsupportedJwtException> { service.parseUserId(refreshToken)}
    }

    @Test
    @DisplayName("access 토큰 재발급 테스트")
    fun given_refreshToken_when_reissueToken_then_returnOnlyAccessToken(){
        val token = service.createAccessAndRefreshToken(USER_ID)
        val refreshToken = token.refreshToken

        assertNotNull(refreshToken)

        Mockito.`when`(ops.get(refreshToken))
            .thenReturn(USER_ID.toString())

        assertDoesNotThrow {
            assertNull(service.reissueToken(refreshToken).refreshToken)
        }
    }

    @Test
    @DisplayName("만료가 임박한 refresh 토큰 재발급 테스트")
    fun given_refreshTokenReachedReissueExpiration_when_reissueToken_then_success(){
        val refreshToken = Jwts.builder().createMockRefreshToken(properties.secret, 1000*10)

        assertNotNull(refreshToken)

        Mockito.`when`(ops.get(refreshToken))
            .thenReturn(USER_ID.toString())

        val response = service.reissueToken(refreshToken)
        assertNotNull(response.refreshToken)
    }

    @Test
    @DisplayName("만료된 refresh 토큰 재발급 테스트")
    fun given_expiredRefreshToken_when_reissueToken_then_success(){
        val refreshToken = Jwts.builder().createMockRefreshToken(properties.secret, 0)

        assertNotNull(refreshToken)

        Mockito.`when`(ops.get(refreshToken))
            .thenReturn(USER_ID.toString())

        assertThrows<RefreshTokenExpiredException> { service.reissueToken(refreshToken) }
    }

    @Test
    @DisplayName("캐시에 저장되어 있지 않은 refresh 토큰으로 access 토큰 재발급 테스트")
    fun given_notStored_refreshToken_when_reissueToken_then_throw_IllegalArgumentException(){
        val token = service.createAccessAndRefreshToken(USER_ID)
        val refreshToken = token.refreshToken

        assertNotNull(refreshToken)
        assertThrows<IllegalStateException> {
            service.reissueToken(refreshToken)
        }
    }


}



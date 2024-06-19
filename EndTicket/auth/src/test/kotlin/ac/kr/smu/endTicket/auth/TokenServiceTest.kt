package ac.kr.smu.endTicket.auth

import ac.kr.smu.endTicket.auth.domain.exception.RefreshTokenExpiredException
import ac.kr.smu.endTicket.auth.infra.property.JWTProperties
import ac.kr.smu.endTicket.auth.service.TokenService
import ac.kr.smu.endticket.common.redis.test.RedisTestConfig
import ac.kr.smu.endticket.protobuf.AccessToken
import ac.kr.smu.endticket.protobuf.TokenServiceGrpc
import ac.kr.smu.endticket.protobuf.TokenServiceGrpc.TokenServiceBlockingStub
import io.grpc.ManagedChannel
import io.grpc.StatusRuntimeException
import io.grpc.inprocess.InProcessChannelBuilder
import io.jsonwebtoken.ExpiredJwtException
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.UnsupportedJwtException
import io.jsonwebtoken.security.Keys
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.context.annotation.Import
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.core.ValueOperations
import org.springframework.test.annotation.DirtiesContext
import kotlin.test.*

@SpringBootTest(classes = [
    TokenService::class,
    GrpcConfiguration::class
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
    private val properties:JWTProperties
){
    private lateinit var stub: TokenServiceBlockingStub
    private lateinit var channel: ManagedChannel
    companion object{
        private const val USER_ID = 1L
    }

    @BeforeTest
    fun init(){
        setUpGrpc()
        Mockito.`when`(redisTemplate.opsForValue()).thenReturn(ops)
    }
    @AfterTest
    fun reset(){
        shutdownGrpc()
    }

    @Test
    @DisplayName("토큰 검증 테스트")
    @DirtiesContext
    fun given_accessToken_when_validationToken_then_returnResponse(){
        val createToken = service.createAccessAndRefreshToken(USER_ID)
        val response = stub.validateAccessToken(
            AccessToken
                .newBuilder()
                .setToken(createToken.accessToken)
                .build()
        )


        assertEquals(USER_ID, response.userId)
        assertEquals(200, response.status)
    }


    @Test
    @DisplayName("민료된 토큰 검증 테스트")
    @DirtiesContext
    fun given_expiredAccessToken_when_validationToken_then_returnResponseStatus401(){
        val accessToken = Mockito.mock(AccessToken::class.java)
        Mockito
            .`when`(accessToken.token)
            .thenThrow(Mockito.mock(ExpiredJwtException::class.java))

        val response = stub.validateAccessToken(
           accessToken
        )

        assertEquals(401, response.status)
        assertEquals(-1,response.userId)
    }

    @Test
    @DisplayName("잘못된 서명 토큰 검증 테스트")
    @DirtiesContext
    fun given_invalidSignatureAccessToken_when_validationToken_then_returnResponseStatus400(){
        val accessToken = AccessToken.newBuilder().setToken(
            Jwts
            .builder()
            .signWith(Keys.hmacShaKeyFor("invalidKeyinvalidKeyinvalidKeyinvalidKeyinvalidKey".toByteArray()))
            .subject(USER_ID.toString())
            .compact()
        ).build()

        val response = stub.validateAccessToken(accessToken)

        assertEquals(400, response.status)
        assertEquals(-1,response.userId)
    }

    @Test
    @DisplayName("토큰 검증 gRPC 에러 테스트")
    @DirtiesContext
    fun when_validationTokenFailWithStatusRuntimeException_then_returnResponseStatus500(){
        val accessToken = Mockito.mock(AccessToken::class.java)
        Mockito
            .`when`(accessToken.token)
            .thenThrow(Mockito.mock(StatusRuntimeException::class.java))

        val response = stub.validateAccessToken(
            accessToken
        )

        assertEquals(500, response.status)
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
        val token = service
            .createAccessAndRefreshToken(USER_ID)

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
    fun given_notStored_refreshToken_when_reissusToken_then_throw_IllegalArgumentException(){
        val token = service.createAccessAndRefreshToken(USER_ID)
        val refreshToken = token.refreshToken

        assertNotNull(refreshToken)
        assertThrows<IllegalStateException> {
            service.reissueToken(refreshToken)
        }
    }

    private fun setUpGrpc(){
        channel = InProcessChannelBuilder.forName("test").directExecutor().build()
        stub = TokenServiceGrpc.newBlockingStub(channel)
    }

    private fun shutdownGrpc(){
        channel.shutdownNow()
    }
}



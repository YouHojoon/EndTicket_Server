package ac.kr.smu.endTicket

import ac.kr.smu.endTicket.auth.infra.property.JWTProperties
import ac.kr.smu.endTicket.auth.service.TokenService
import ac.kr.smu.endTicket.common.redis.test.RedisTestConfig
import ac.kr.smu.endTicket.protobuf.AccessToken
import ac.kr.smu.endTicket.protobuf.TokenServiceGrpc
import ac.kr.smu.endTicket.protobuf.TokenServiceGrpc.TokenServiceBlockingStub
import io.grpc.ManagedChannel
import io.grpc.Server
import io.grpc.inprocess.InProcessChannelBuilder
import io.grpc.inprocess.InProcessServerBuilder
import io.jsonwebtoken.UnsupportedJwtException
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
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig
import kotlin.test.*

@SpringBootTest
@TestPropertySource(
    locations = ["/application.yml"]
)
@SpringJUnitConfig(
    TokenService::class
)
@Import(RedisTestConfig::class)
@EnableConfigurationProperties(JWTProperties::class)
class TokenServiceTest @Autowired constructor(
    @MockBean
    private val ops: ValueOperations<String,String>,
    @MockBean
    private val redisTemplate: RedisTemplate<String,String>,
    private val service: TokenService
){
    private lateinit var stub: TokenServiceBlockingStub
    private lateinit var channel: ManagedChannel
    private lateinit var server: Server
    companion object{
        private const val USER_ID = 1L
    }

    @BeforeTest
    fun init(){
        Mockito.`when`(redisTemplate.opsForValue()).thenReturn(ops)
    }

    @Test
    @DisplayName("grpc 토큰 검증 테스트")
    fun given_accessToken_when_validationToken_then_returnResponse(){
        val createToken = service.createAccessAndRefreshToken(USER_ID)
        setGrpc()
        val response = stub.validateAccessToken(
            AccessToken.newBuilder()
                .setToken(createToken.accessToken)
                .build()
        )

        assertNotNull(response.userID)
        assertEquals(response.userID, USER_ID)
        shutdownGrpc()
    }
    @Test
    @DisplayName("grpc 리프레시 토큰으로 토큰 검증 테스트")
    fun given_refreshToken_when_validationToken_then_returnResponseOfStatus400(){
        setGrpc()
        val createTokenResponse = service.createAccessAndRefreshToken(USER_ID)
        val response = stub.validateAccessToken(
            AccessToken.newBuilder()
                .setToken(createTokenResponse.refreshToken)
                .build()
        )
        println(response.userID)
        assertEquals(response.userID, -1)
        assertEquals(response.status, 400)
        shutdownGrpc()
    }
    @Test
    @DisplayName("정상 유저 토큰 발급 테스트")
    fun given_userID_when_createAccessAndRefreshToken_then_success(){
        assertDoesNotThrow {
            service.createAccessAndRefreshToken(userID = USER_ID)
        }
    }

    @Test
    @DisplayName("access 토큰으로 사용자 ID 파싱")
    fun given_accessToken_when_parseUserID_then_return_UserID() {
        val token = service
            .createAccessAndRefreshToken(USER_ID)

        assertEquals(service.parseUserID(token.accessToken), USER_ID)
    }

    @Test
    @DisplayName("refresh 토큰으로 사용자 ID 파싱 테스트")
    fun given_refreshToken_when_parseUserID_then_throw_UnsupportedJwtException(){
        val token = service.createAccessAndRefreshToken(USER_ID)
        val refreshToken = token.refreshToken

        assertNotNull(refreshToken)
        assertThrows<UnsupportedJwtException> { service.parseUserID(refreshToken)}
    }

    @Test
    @DisplayName("access 토큰 재발급 테스트")
    fun given_refreshToken_when_reissueToken_then_success(){
        val token = service.createAccessAndRefreshToken(USER_ID)
        val refreshToken = token.refreshToken

        assertNotNull(refreshToken)

        Mockito.`when`(ops.get(refreshToken))
            .thenReturn(USER_ID.toString())

        assertDoesNotThrow {service.reissueToken(refreshToken)}
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

    private fun setGrpc(){
        val name = InProcessServerBuilder.generateName()
        server = InProcessServerBuilder.forName(name)
            .directExecutor().addService(service)
            .build().start()

        channel = InProcessChannelBuilder.forName(name).directExecutor().build()
        stub = TokenServiceGrpc.newBlockingStub(channel)
    }

    private fun shutdownGrpc(){
        server.shutdownNow()
        channel.shutdownNow()
    }

}



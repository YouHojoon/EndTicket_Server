package ac.kr.smu.endTicket

import ac.kr.smu.endTicket.auth.domain.service.UserService
import ac.kr.smu.endTicket.auth.service.TokenService
import ac.kr.smu.endTicket.auth.infra.config.JWTProperties
import ac.kr.smu.protobuf.AccessToken
import ac.kr.smu.protobuf.TokenServiceGrpc
import ac.kr.smu.protobuf.TokenServiceGrpc.TokenServiceBlockingStub
import io.grpc.inprocess.InProcessChannelBuilder
import io.grpc.inprocess.InProcessServerBuilder
import io.grpc.testing.GrpcCleanupRule
import io.jsonwebtoken.UnsupportedJwtException
import org.junit.Rule
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.core.ValueOperations
import org.springframework.test.context.ActiveProfiles
import kotlin.test.*

@SpringBootTest()
@ActiveProfiles("test")
@EnableConfigurationProperties(JWTProperties::class)
class TokenServiceTest @Autowired constructor(
    @MockBean
    private val ops: ValueOperations<String,String>,
    @MockBean
    private val redisTemplate: RedisTemplate<String,String>,
    @MockBean
    private val userService:UserService,

    private val service: TokenService
){
    @Rule
    private val cleanupRule: GrpcCleanupRule = GrpcCleanupRule()
    private lateinit var stub: TokenServiceBlockingStub
    private val USER_ID = 1L

    @BeforeTest
    fun setStub(){
        val server = InProcessServerBuilder.generateName()
        cleanupRule.register(
            InProcessServerBuilder.forName(server)
                .directExecutor().addService(service)
                .build().start()
        )
        stub = TokenServiceGrpc.newBlockingStub(
            cleanupRule.register(
            InProcessChannelBuilder.forName(server).directExecutor().build()
        ))
    }
    @BeforeTest
    fun setRedis(){
        Mockito.`when`(redisTemplate.opsForValue()).thenReturn(ops)
    }

    @Test
    @DisplayName("grpc 토큰 검증 테스트")
    fun given_accessToken_when_validationToken_then_returnResponse(){
        val createToken = service.createAccessAndRefreshToken(USER_ID)

        val response = stub.validateAccessToken(
            AccessToken.newBuilder()
                .setToken(createToken.accessToken)
                .build()
        )

        assertNotNull(response.userID)
        assertEquals(response.userID, USER_ID)
    }
    @Test
    @DisplayName("grpc 리프레시로 토큰 검증 테스트")
    fun given_refreshToken_when_validationToken_then_returnResponseOfStatus400(){
        val createTokenResponse = service.createAccessAndRefreshToken(USER_ID)
        val response = stub.validateAccessToken(
            AccessToken.newBuilder()
                .setToken(createTokenResponse.refreshToken)
                .build()
        )

        assertEquals(response.userID, -1)
        assertEquals(response.status, 400)
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

        assertThrows<UnsupportedJwtException> { service.parseUserID(token.refreshToken)}
    }

    @Test
    @DisplayName("access 토큰 재발급 테스트")
    fun given_refreshToken_when_reissueToken_then_success(){
        val token = service.createAccessAndRefreshToken(USER_ID)

        Mockito.`when`(ops.get(token.refreshToken))
            .thenReturn(USER_ID.toString())

        assertDoesNotThrow {service.reissueToken(token.refreshToken)}
    }

    @Test
    @DisplayName("캐시에 저장되어 있지 않은 refresh 토큰으로 access 토큰 재발급 테스트")
    fun given_notStored_refreshToken_when_reissusToken_then_throw_IllegalArgumentException(){
        val token = service.createAccessAndRefreshToken(USER_ID)

        assertThrows<IllegalArgumentException> {
            service.reissueToken(token.refreshToken)
        }
    }
}



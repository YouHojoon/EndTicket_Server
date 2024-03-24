package ac.kr.smu.endTicket

import ac.kr.smu.endTicket.auth.service.TokenService
import ac.kr.smu.endTicket.infra.config.JWTProperties
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
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.core.ValueOperations
import org.springframework.test.context.ActiveProfiles
import kotlin.test.BeforeTest
import kotlin.test.assertEquals

@SpringBootTest
@ActiveProfiles("test")
@EnableConfigurationProperties(JWTProperties::class)
class TokenServiceTest @Autowired constructor(
    @MockBean
    private val ops: ValueOperations<String,String>,
    @MockBean
    private val redisTemplate: RedisTemplate<String,String>,
    private val service: TokenService
){
    private val USER_ID = 1L
    @BeforeTest
    fun setRedis(){
        Mockito.`when`(redisTemplate.opsForValue()).thenReturn(ops)
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



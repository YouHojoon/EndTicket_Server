package ac.kr.smu.endTicket.auth

import ac.kr.smu.endTicket.auth.domain.model.SocialType
import ac.kr.smu.endTicket.auth.service.UserService
import io.github.resilience4j.circuitbreaker.CircuitBreaker
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry
import io.github.resilience4j.springboot3.circuitbreaker.autoconfigure.CircuitBreakerAutoConfiguration
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.aop.AopAutoConfiguration
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.annotation.DirtiesContext
import kotlin.test.assertEquals

@SpringBootTest(
    classes = [
        UserService::class,
        GrpcConfiguration::class,
        CircuitBreakerAutoConfiguration::class,
        AopAutoConfiguration::class
    ]
)
@DirtiesContext
class UserServiceTest @Autowired constructor(
    private val service: UserService,
    private val registry: CircuitBreakerRegistry

) {
    companion object{
        const val USER_ID = 1L
    }

    @Test
    @DisplayName("gRPC를 통한 userID 수신 테스트")
    @DirtiesContext
    fun given_socialTypeAndSocialUserNumber_when_findUserID_then_returnFindUserID(){
        val socialType = SocialType.KAKAO
        val socialUserNumber = "1"

        assertEquals(USER_ID,  service.findUserID(socialType,socialUserNumber))
    }

    @Test
    @DisplayName("fallback 메소드 테스트")
    @DirtiesContext
    fun when_findUserIDThrowException_then_runFallback(){
        val socialType = SocialType.KAKAO
        val socialUserNumber = "2"
        val breaker = registry.circuitBreaker("find-user-id")

        assertEquals(-1, service.findUserID(socialType,socialUserNumber))
        assertEquals(CircuitBreaker.State.OPEN,breaker.state)
    }
}
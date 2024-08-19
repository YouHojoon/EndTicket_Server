//package ac.kr.smu.endticket.auth
//
//import ac.kr.smu.endticket.auth.service.UserService
//import io.github.resilience4j.circuitbreaker.CircuitBreaker
//import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry
//import io.github.resilience4j.springboot3.circuitbreaker.autoconfigure.CircuitBreakerAutoConfiguration
//import org.junit.jupiter.api.DisplayName
//import org.junit.jupiter.api.Test
//import org.junit.jupiter.api.assertThrows
//import org.springframework.beans.factory.annotation.Autowired
//import org.springframework.boot.autoconfigure.aop.AopAutoConfiguration
//import org.springframework.boot.test.context.SpringBootTest
//import org.springframework.test.annotation.DirtiesContext
//import kotlin.test.assertEquals
//
//@SpringBootTest(
//    classes = [
//        UserService::class,
//        GrpcConfig::class,
//        CircuitBreakerAutoConfiguration::class,
//        AopAutoConfiguration::class,
//    ],
//)
//@DirtiesContext
//class UserServiceTest
//    @Autowired
//    constructor(
//        private val service: UserService,
//        private val registry: CircuitBreakerRegistry,
//    ) {
//        @Test
//        @DisplayName("gRPC를 통한 userID 수신 테스트")
//        @DirtiesContext
//        fun given_socialTypeAndSocialUserNumber_when_findUserID_then_returnUserId() {
//            assertEquals(
//                AuthTestParameters.USER_ID,
//                service
//                    .findUserId(AuthTestParameters.SOCIAL_TYPE, AuthTestParameters.SOCIAL_USER_NUMBER)
//                    .get(),
//            )
//        }
//
//        @Test
//        @DisplayName("fallback 메소드 테스트")
//        @DirtiesContext
//        fun when_findUserIDThrowException_then_runFallback() {
//            val breaker = registry.circuitBreaker("find-user-id")
//
//            assertThrows<Exception> { service.findUserId(AuthTestParameters.SOCIAL_TYPE, "2").get() }
//            assertEquals(CircuitBreaker.State.OPEN, breaker.state)
//        }
//    }

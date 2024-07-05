package ac.kr.smu.endTicket.auth.service

import ac.kr.smu.endTicket.auth.domain.model.SocialType
import ac.kr.smu.endticket.protobuf.FindUserIdRequest
import ac.kr.smu.endticket.protobuf.UserServiceGrpc
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker
import io.github.resilience4j.timelimiter.annotation.TimeLimiter
import net.devh.boot.grpc.client.inject.GrpcClient
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.util.concurrent.CompletableFuture

/**
 * User 서버와 gRPC를 통해 통신하는 객체
 */
@Service
class UserService {
    @GrpcClient("user")
    private lateinit var userStub: UserServiceGrpc.UserServiceBlockingStub
    private val log = LoggerFactory.getLogger(UserService::class.java)

    /**
     * 사용자 번호를 반환하는 메소드, 만약 가입이 되어 있지 않다면 가입된다.
     * @param socialType 가입된 SNS 종류
     * @param socialUserNumber 해당 SNS의 사용자 번호
     * @return 사용자 번호
     */

    @CircuitBreaker(name = "find-user-id", fallbackMethod = "fallbackFindUserId")
    @TimeLimiter(name = "find-user-id")
    fun findUserId(
        socialType: SocialType,
        socialUserNumber: String,
    ): CompletableFuture<Long> =
        CompletableFuture.completedFuture(
            userStub
                .findUserId(
                    FindUserIdRequest
                        .newBuilder()
                        .setSocialType(
                            ac.kr.smu.endticket.protobuf.SocialType
                                .valueOf(socialType.name),
                        ).setSocialUserNumber(socialUserNumber)
                        .build(),
                ).userId,
        )

    /**
     * findUserId의 fallback 메소드
     * @param socialType 실패한 사용의 SNS 종류
     * @param socialUserNumber 실패한 사용자의 SNS 사용자 번호
     * @param e 발생한 에러
     * @return -1 반환
     */
    private fun fallbackFindUserId(
        socialType: SocialType,
        socialUserNumber: String,
        e: Exception,
    ): CompletableFuture<Long> {
        log.error("{socialType: $socialType, socialUserNumber: $socialUserNumber}", e)

        return CompletableFuture.failedFuture(e)
    }
}

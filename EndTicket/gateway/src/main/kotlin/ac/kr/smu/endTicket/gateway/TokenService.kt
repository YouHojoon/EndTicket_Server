package ac.kr.smu.endTicket.gateway

import ac.kr.smu.endticket.protobuf.AccessToken
import ac.kr.smu.endticket.protobuf.TokenServiceGrpc.TokenServiceBlockingStub
import ac.kr.smu.endticket.protobuf.ValidateAccessTokenResponse
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker
import io.github.resilience4j.timelimiter.annotation.TimeLimiter
import net.devh.boot.grpc.client.inject.GrpcClient
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.util.concurrent.CompletableFuture

/**
 * grpc를 이용해 auth 서비스와 통신하는 클래스
 */
@Service
class TokenService {
    @GrpcClient("auth")
    private lateinit var stub: TokenServiceBlockingStub
    private val log = LoggerFactory.getLogger(TokenService::class.java)

    /**
     * 토큰을 검증하는 메소드
     * @param accessToken access token
     * @return 검증 결과
     */
    @CircuitBreaker(name = "validate-access-token", fallbackMethod = "fallbackValidateAccessToken")
    @TimeLimiter(name = "validate-access-token")
    fun validateAccessToken(accessToken: String): CompletableFuture<ValidateAccessTokenResponse> =
        CompletableFuture.completedFuture(
            stub.validateAccessToken(
                AccessToken.newBuilder().setToken(accessToken).build(),
            ),
        )

    private fun fallbackValidateAccessToken(
        token: String,
        e: Exception,
    ): CompletableFuture<ValidateAccessTokenResponse> {
        val message = "auth 서버에 access 토큰 검증 요청 실패 : {token: $token}"
        log.error(message, e)

        return CompletableFuture.failedFuture(e)
    }
}

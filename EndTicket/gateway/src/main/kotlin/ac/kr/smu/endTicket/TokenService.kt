package ac.kr.smu.endTicket

import ac.kr.smu.protobuf.AccessToken
import ac.kr.smu.protobuf.TokenServiceGrpc.TokenServiceBlockingStub
import ac.kr.smu.protobuf.ValidateAccessTokenResponse
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker
import net.devh.boot.grpc.client.inject.GrpcClient
import org.slf4j.LoggerFactory
import org.slf4j.MDC
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service

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
    @CircuitBreaker(name = "validate-accessToken", fallbackMethod = "fallbackValidateAccessToken")
    fun validateAccessToken(accessToken: String): ValidateAccessTokenResponse{
        return stub.validateAccessToken(AccessToken.newBuilder().setToken(accessToken).build())
    }

    private fun fallbackValidateAccessToken(token: String, e: Exception): ValidateAccessTokenResponse{
        val message = "auth 서버에 access 토큰 검증 요청 실패"
        MDC.put("accessToken", token)
        log.error(message, e)
        MDC.clear()
        return ValidateAccessTokenResponse
            .newBuilder()
            .setUserID(-1)
            .setStatus(HttpStatus.SERVICE_UNAVAILABLE.value())
            .setMessage(message).build()
    }
}
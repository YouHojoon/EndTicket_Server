package ac.kr.smu.endTicket.gateway

import ac.kr.smu.endticket.common.constant.HttpHeaderName
import ac.kr.smu.endticket.common.web.response.ExceptionResponse
import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.cloud.gateway.filter.GatewayFilter
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.server.reactive.ServerHttpResponse
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

@Component
class AuthorizationFilter(
    private val tokenService: TokenService,
) : AbstractGatewayFilterFactory<Any>() {
    private val log = LoggerFactory.getLogger(AuthorizationFilter::class.java)

    override fun apply(config: Any): GatewayFilter {
        return GatewayFilter { exchange, chain ->
            val message = "게이트웨이 인증 에러"
            val token =
                exchange.request.headers.getFirst("Authorization")
                    ?: return@GatewayFilter denyRequest(
                        exchange.response,
                        HttpStatus.BAD_REQUEST,
                        ExceptionResponse(400, message, "access 토큰이 없습니다."),
                    )

            try {
                val response = tokenService.validateAccessToken(token).get()

                if (response.status == 200) {
                    val request =
                        exchange.request
                            .mutate()
                            .header(HttpHeaderName.USER_ID, response.userId.toString())
                            .build()
                    chain
                        .filter(exchange.mutate().request(request).build())
                } else {
                    denyRequest(
                        exchange.response,
                        HttpStatus.valueOf(response.status),
                        ExceptionResponse(
                            code = response.status,
                            message = message,
                            detail = response.message ?: "",
                        ),
                    )
                }
            } catch (e: Exception) {
                log.error("인증 서버와 통신 실패", e)
                denyRequest(
                    exchange.response,
                    HttpStatus.SERVICE_UNAVAILABLE,
                    ExceptionResponse(503, message, "인증 서버와 통신에 실패했습니다."),
                )
            }
        }
    }

    private fun denyRequest(
        response: ServerHttpResponse,
        status: HttpStatus,
        body: ExceptionResponse,
    ): Mono<Void> {
        response.statusCode = status
        response.headers.contentType = MediaType.APPLICATION_JSON

        return response.writeWith(Mono.just(response.bufferFactory().wrap(ObjectMapper().writeValueAsBytes(body))))
    }
}

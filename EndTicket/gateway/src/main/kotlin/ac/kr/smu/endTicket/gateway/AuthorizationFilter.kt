package ac.kr.smu.endTicket.gateway

import ac.kr.smu.endticket.common.constant.HttpHeaderName
import ac.kr.smu.endticket.common.web.response.ExceptionResponse
import com.fasterxml.jackson.databind.ObjectMapper
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
    override fun apply(config: Any): GatewayFilter {
        return GatewayFilter { exchange, chain ->
            val token =
                exchange.request.headers.getFirst("Authorization")
                    ?: return@GatewayFilter denyRequest(
                        exchange.response,
                        HttpStatus.UNAUTHORIZED,
                        ExceptionResponse(401, "게이트웨이 인증 에러", "access 토큰이 없습니다."),
                    )

            try {
                val userId = tokenService.validateAccessToken(token).get().userId
                val request =
                    exchange.request
                        .mutate()
                        .header(HttpHeaderName.USER_ID, userId.toString())
                        .build()
                chain
                    .filter(exchange.mutate().request(request).build())
            } catch (e: Exception) {
                denyRequest(
                    exchange.response,
                    HttpStatus.UNAUTHORIZED,
                    ExceptionResponse(HttpStatus.UNAUTHORIZED.value(), "게이트웨이 인증 에러", "인증에 실패했습니다."),
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

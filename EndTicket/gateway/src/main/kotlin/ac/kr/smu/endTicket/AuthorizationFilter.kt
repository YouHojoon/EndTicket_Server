package ac.kr.smu.endTicket

import ErrorResponse
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.cloud.gateway.filter.GatewayFilter
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.server.reactive.ServerHttpResponse
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import reactor.core.publisher.Mono

@Component
class AuthorizationFilter(
    private val authService: AuthService,
): AbstractGatewayFilterFactory<Any>() {
    private val USER_ID_HEADER_NAME = "User-ID"
    override fun apply(config: Any): GatewayFilter {
        return GatewayFilter { exchange, chain ->
            val token = exchange.request.headers.getFirst("Authorization")
                ?: return@GatewayFilter denyRequest(exchange.response, HttpStatus.UNAUTHORIZED,ErrorResponse(401, "게이트웨이 인증 에러", "access 토큰이 없습니다."))
            val response = authService.validationToken(token)
            val userID = response.userID
            
            if(userID != null) {
                val request = exchange.request.mutate().header(USER_ID_HEADER_NAME, userID.toString()).build()
                chain.filter(exchange.mutate().request(request).build())
            }

            else
                denyRequest(exchange.response, HttpStatus.valueOf(response.status), ErrorResponse(response.status, "게이트웨이 인증 에러", response.message))
        }
    }

    private fun denyRequest(response: ServerHttpResponse, status: HttpStatus, body: ErrorResponse): Mono<Void>{
        response.statusCode = status
        response.headers.contentType = MediaType.APPLICATION_JSON

        return response.writeWith(Mono.just(response.bufferFactory().wrap(ObjectMapper().writeValueAsBytes(body))))
    }
}
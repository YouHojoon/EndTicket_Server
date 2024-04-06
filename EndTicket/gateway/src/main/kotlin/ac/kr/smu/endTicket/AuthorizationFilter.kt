package ac.kr.smu.endTicket

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
    private val webClientBuilder: WebClient.Builder
): AbstractGatewayFilterFactory<Any>() {
    private val AUTHORIZATION_URL = "http://auth/auth/validation"
    override fun apply(config: Any): GatewayFilter {
        return GatewayFilter { exchange, chain ->
            val token = exchange.request.headers.getFirst("Authorization")
                ?: return@GatewayFilter denyRequest(exchange.response, HttpStatus.UNAUTHORIZED, "access 토큰이 없습니다.".toByteArray())
            val response = authService.validationToken(token)

            when(response.status){
                200 -> chain.filter(exchange)
                else -> {
                    denyRequest(exchange.response, HttpStatus.valueOf(response.status))
                }
            }

//            webClientBuilder
//                .baseUrl(AUTHORIZATION_URL)
//                .build()
//                .post()
//                .header("Authorization", token)
//                .retrieve()
//                .toBodilessEntity()
//                .flatMap {
//                    chain.filter(exchange)
//                }
//                .onErrorResume {
//                    if (it is WebClientResponseException)
//                       denyRequest(exchange.response, HttpStatus.valueOf(it.statusCode.value()), it.responseBodyAsByteArray)
//                    else
//                        denyRequest(exchange.response, HttpStatus.UNAUTHORIZED, it.message?.toByteArray())
//                }
        }
    }

    private fun denyRequest(response: ServerHttpResponse, status: HttpStatus, body: ByteArray? = null): Mono<Void>{
        response.statusCode = status
        response.headers.contentType = MediaType.APPLICATION_JSON

        return response.writeWith(Mono.just(response.bufferFactory().wrap(body ?: ByteArray(0))))
    }
}
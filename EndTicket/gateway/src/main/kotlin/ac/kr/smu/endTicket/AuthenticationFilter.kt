package ac.kr.smu.endTicket

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.cloud.client.discovery.DiscoveryClient
import org.springframework.cloud.gateway.filter.GatewayFilter
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.server.reactive.ServerHttpResponse
import org.springframework.stereotype.Component
import org.springframework.web.ErrorResponse
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import org.springframework.web.reactive.function.client.awaitBodilessEntity
import reactor.core.publisher.Mono

@Component
class AuthenticationFilter(
    private val webClientBuilder: WebClient.Builder
): AbstractGatewayFilterFactory<AuthenticationFilter.Config>(Config::class.java) {

    override fun apply(config: Config): GatewayFilter {
        return GatewayFilter { exchange, chain ->
            if (exchange.request.uri.toString() in config.whiteList){
                return@GatewayFilter chain.filter(exchange)
            }

            val token = exchange.request.headers.getFirst("Authorization")
                ?: return@GatewayFilter denyRequest(
                    response = exchange.response,
                    status = HttpStatus.UNAUTHORIZED,
                    body = "access 토큰이 없습니다.".toByteArray())

            webClientBuilder
                .baseUrl("http://auth/auth/validation")
                .build()
                .post()
                .header("Authorization", token)
                .retrieve()
                .toBodilessEntity()
                .flatMap {
                    chain.filter(exchange)
                }
                .onErrorResume {
                    if (it is WebClientResponseException)
                       denyRequest(exchange.response, HttpStatus.valueOf(it.statusCode.value()), it.responseBodyAsByteArray)
                    else
                        denyRequest(exchange.response, HttpStatus.UNAUTHORIZED, it.message?.toByteArray())
                }
        }
    }

    private fun denyRequest(response: ServerHttpResponse, status: HttpStatus, body: ByteArray?): Mono<Void>{
        response.statusCode = status
        response.headers.contentType = MediaType.APPLICATION_JSON

        return response.writeWith(Mono.just(response.bufferFactory().wrap(body ?: ByteArray(0))))
    }

    data class Config(val whiteList: List<String>)
}
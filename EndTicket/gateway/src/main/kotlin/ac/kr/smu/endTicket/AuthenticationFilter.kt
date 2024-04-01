package ac.kr.smu.endTicket

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.cloud.client.discovery.DiscoveryClient
import org.springframework.cloud.gateway.filter.GatewayFilter
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory
import org.springframework.http.HttpStatus
import org.springframework.http.server.reactive.ServerHttpResponse
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import org.springframework.web.reactive.function.client.awaitBodilessEntity
import reactor.core.publisher.Mono

@Component
class AuthenticationFilter: AbstractGatewayFilterFactory<AuthenticationFilter.Config>() {

    override fun apply(config: Config): GatewayFilter {
        return GatewayFilter { exchange, chain ->
            if (exchange.request.uri.toString() in config.whitelist){
                return@GatewayFilter chain.filter(exchange)
            }

            val token = exchange.request.headers.getFirst("Authorization") ?: return@GatewayFilter denyRequest(exchange.response, HttpStatus.UNAUTHORIZED, "access 토큰이 없습니다.")

            WebClient
                .create("http://auth")
                .post()
                .header("Authorization", token)
                .retrieve()
                .toBodilessEntity()
                .flatMap {
                    chain.filter(exchange)
                }
                .onErrorResume {
                    if (it is WebClientResponseException){
                        denyRequest(exchange.response,
                            HttpStatus.valueOf(it.statusCode.value()), it.message)
                    }
                    else{
                        denyRequest(exchange.response, HttpStatus.UNAUTHORIZED, it.message)
                    }
                }
        }
    }

    private fun denyRequest(response: ServerHttpResponse, status: HttpStatus, message: String?): Mono<Void>{
        val body = ObjectMapper().writeValueAsString(mapOf("message" to message, "code" to status.value()))
        response.statusCode = status
        return response.writeWith {
            it.onNext(
                response.bufferFactory().wrap(body.toByteArray())
            )
        }
    }

    data class Config(val whitelist: List<String>)
}
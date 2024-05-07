package ac.kr.smu.endTicket

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cloud.client.discovery.EnableDiscoveryClient
import org.springframework.cloud.gateway.route.Route
import org.springframework.cloud.gateway.route.RouteLocator
import org.springframework.cloud.gateway.route.builder.*
import org.springframework.context.annotation.Bean
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.web.cors.reactive.CorsUtils
import org.springframework.web.server.WebFilter
import reactor.core.publisher.Hooks


@SpringBootApplication
@EnableDiscoveryClient
class GatewayApplication{
    @Bean
    fun corsFilter(): WebFilter{
        return WebFilter { exchange, chain ->
            val request = exchange.request

            if (CorsUtils.isCorsRequest(request)){
                val headers = exchange.response.headers

                headers.accessControlAllowOrigin = "http://localhost:8083"
                headers.accessControlAllowMethods = listOf(HttpMethod.GET, HttpMethod.POST, HttpMethod.PUT, HttpMethod.PATCH, HttpMethod.OPTIONS, HttpMethod.HEAD)
                headers.accessControlAllowCredentials = true
                headers.accessControlAllowHeaders = listOf("Content-Type", "X-User-ID", "Authorization")

                if (request.method == HttpMethod.OPTIONS){
                    exchange.response.setStatusCode(HttpStatus.OK)
                    return@WebFilter exchange.response.setComplete()
                }
            }

            chain.filter(exchange)
        }
    }

    @Bean
    fun routeLocator(builder: RouteLocatorBuilder): RouteLocator{
        return builder.routes {
            serviceAPIDocsRoute("auth")
            serviceAPIDocsRoute("user")
            serviceAPIDocsRoute("ticket")
        }
    }

    private fun RouteLocatorDsl.serviceAPIDocsRoute(service:String){
        route {
            path("/$service/api-docs")
                .filters {
                    it.rewritePath("/${service}/api-docs","/api-docs")
                }
                .uri("lb://$service")
        }
    }
}



fun main(args: Array<String>) {
    runApplication<GatewayApplication>(*args)
    Hooks.enableAutomaticContextPropagation()
}
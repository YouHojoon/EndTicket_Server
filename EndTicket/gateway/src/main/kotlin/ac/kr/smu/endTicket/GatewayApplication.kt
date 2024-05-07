package ac.kr.smu.endTicket

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cloud.client.discovery.EnableDiscoveryClient
import org.springframework.cloud.gateway.route.Route
import org.springframework.cloud.gateway.route.RouteLocator
import org.springframework.cloud.gateway.route.builder.*
import org.springframework.context.annotation.Bean
import reactor.core.publisher.Hooks
import java.net.URI
import java.util.Optional

@SpringBootApplication
@EnableDiscoveryClient
class GatewayApplication{
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
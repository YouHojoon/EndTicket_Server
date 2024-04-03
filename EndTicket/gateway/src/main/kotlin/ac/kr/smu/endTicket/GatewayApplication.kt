package ac.kr.smu.endTicket

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

import org.springframework.cloud.client.discovery.EnableDiscoveryClient
import org.springframework.cloud.client.loadbalancer.LoadBalanced
import org.springframework.context.annotation.Bean
import org.springframework.web.reactive.function.client.WebClient

@SpringBootApplication
@EnableDiscoveryClient
class GatewayApplication{
    @Bean
    @LoadBalanced
    fun loadBalancedWebClient(): WebClient.Builder{
        return WebClient.builder()
    }
}

fun main(args: Array<String>) {
    runApplication<GatewayApplication>(*args)
}

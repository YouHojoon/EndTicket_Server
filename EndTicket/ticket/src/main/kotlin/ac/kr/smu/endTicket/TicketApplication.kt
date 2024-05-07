package ac.kr.smu.endTicket

import ac.kr.smu.endTicket.ticket.infra.config.property.RedisClusterProperties
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication
import org.springframework.cache.annotation.EnableCaching
import org.springframework.cloud.client.discovery.EnableDiscoveryClient
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@EnableDiscoveryClient
@EnableCaching
@EnableConfigurationProperties(RedisClusterProperties::class)
@EnableScheduling
class TicketApplication
fun main(args: Array<String>) {
    runApplication<TicketApplication>(*args)
}
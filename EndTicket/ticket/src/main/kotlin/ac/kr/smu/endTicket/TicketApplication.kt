package ac.kr.smu.endTicket

import ac.kr.smu.endTicket.annotation.EnableAutoRedisConfig
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cloud.client.discovery.EnableDiscoveryClient
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@EnableDiscoveryClient
@EnableScheduling
@EnableAutoRedisConfig
class TicketApplication
fun main(args: Array<String>) {
    runApplication<TicketApplication>(*args)
}
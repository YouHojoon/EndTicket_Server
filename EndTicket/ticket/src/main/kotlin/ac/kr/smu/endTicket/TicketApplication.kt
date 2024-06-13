package ac.kr.smu.endTicket

import ac.kr.smu.endTicket.common.redis.annotation.EnableAutoRedisConfig
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cloud.client.discovery.EnableDiscoveryClient
import org.springframework.data.jpa.repository.config.EnableJpaAuditing
import org.springframework.scheduling.annotation.EnableAsync
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@EnableDiscoveryClient
@EnableScheduling
@EnableAutoRedisConfig
@EnableJpaAuditing
@EnableAsync
class TicketApplication
fun main(args: Array<String>) {
    runApplication<TicketApplication>(*args)
}
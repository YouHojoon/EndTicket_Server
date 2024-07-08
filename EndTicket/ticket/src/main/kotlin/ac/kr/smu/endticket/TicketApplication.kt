package ac.kr.smu.endticket

import ac.kr.smu.endticket.common.web.aop.BindExceptionAdvice
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cloud.client.discovery.EnableDiscoveryClient
import org.springframework.context.annotation.Import
import org.springframework.data.jpa.repository.config.EnableJpaAuditing
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@EnableDiscoveryClient
@EnableScheduling
@EnableJpaAuditing
@Import(BindExceptionAdvice::class)
class TicketApplication

fun main(args: Array<String>) {
    runApplication<TicketApplication>(*args)
}

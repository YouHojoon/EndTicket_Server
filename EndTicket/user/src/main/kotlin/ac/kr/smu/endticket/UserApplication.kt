package ac.kr.smu.endticket

import ac.kr.smu.endticket.common.web.aop.BindExceptionAdvice
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cloud.client.discovery.EnableDiscoveryClient
import org.springframework.context.annotation.Import
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@EnableDiscoveryClient
@EnableScheduling
@Import(BindExceptionAdvice::class)
class UserApplication

fun main(args: Array<String>) {
    runApplication<UserApplication>(*args)
}

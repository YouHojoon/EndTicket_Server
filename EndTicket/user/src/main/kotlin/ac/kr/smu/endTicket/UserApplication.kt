package ac.kr.smu.endTicket

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cloud.client.discovery.EnableDiscoveryClient
import org.springframework.cloud.openfeign.EnableFeignClients

@SpringBootApplication
@EnableDiscoveryClient
class UserApplication

fun main(args: Array<String>) {
    runApplication<UserApplication>(*args)
}

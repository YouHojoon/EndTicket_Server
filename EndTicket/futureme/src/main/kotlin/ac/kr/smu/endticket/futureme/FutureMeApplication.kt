package ac.kr.smu.endticket.futureme

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cloud.client.discovery.EnableDiscoveryClient
import org.springframework.data.jpa.repository.config.EnableJpaAuditing

@SpringBootApplication
@EnableDiscoveryClient
@EnableJpaAuditing
class FutureMeApplication

fun main(args: Array<String>){
    runApplication<FutureMeApplication>(*args)
}
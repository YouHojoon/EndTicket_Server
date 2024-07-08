package ac.kr.smu.endticket.futureme

import ac.kr.smu.endticket.common.web.aop.BindExceptionAdvice
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cloud.client.discovery.EnableDiscoveryClient
import org.springframework.context.annotation.Import
import org.springframework.data.jpa.repository.config.EnableJpaAuditing

@SpringBootApplication
@EnableDiscoveryClient
@EnableJpaAuditing
@Import(BindExceptionAdvice::class)
class FutureMeApplication

fun main(args: Array<String>){
    runApplication<FutureMeApplication>(*args)
}
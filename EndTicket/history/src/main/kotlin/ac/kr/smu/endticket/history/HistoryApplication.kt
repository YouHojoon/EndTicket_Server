package ac.kr.smu.endticket.history

import ac.kr.smu.endticket.common.redis.annotation.EnableAutoRedisConfig
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cloud.client.discovery.EnableDiscoveryClient
import org.springframework.data.jpa.repository.config.EnableJpaAuditing


@SpringBootApplication
@EnableJpaAuditing
@EnableDiscoveryClient
@EnableAutoRedisConfig
class HistoryApplication
fun main(args: Array<String>){
    runApplication<HistoryApplication>(*args)
}
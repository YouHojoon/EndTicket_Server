package ac.kr.smu.endticket

import ac.kr.smu.endticket.common.redis.annotation.EnableAutoRedisConfig
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@EnableAutoRedisConfig
@SpringBootApplication
class AuthApplication

fun main(args: Array<String>) {
    runApplication<AuthApplication>(*args)
}

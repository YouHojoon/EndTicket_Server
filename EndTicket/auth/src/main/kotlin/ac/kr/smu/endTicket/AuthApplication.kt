package ac.kr.smu.endTicket

import ac.kr.smu.endticket.common.redis.annotation.EnableAutoRedisConfig
import ac.kr.smu.endTicket.auth.infra.property.JWTProperties
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication

@EnableConfigurationProperties(JWTProperties::class)
@EnableAutoRedisConfig
@SpringBootApplication
class AuthApplication

fun main(args: Array<String>) {
    runApplication<AuthApplication>(*args)
}

package ac.kr.smu.endTicket

import ac.kr.smu.endTicket.annotation.EnableAutoRedisConfig
import ac.kr.smu.endTicket.auth.infra.property.JWTProperties
import ac.kr.smu.endTicket.property.RedisClusterProperties
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
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

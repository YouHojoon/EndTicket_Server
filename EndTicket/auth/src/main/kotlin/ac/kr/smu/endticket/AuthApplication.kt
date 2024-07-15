package ac.kr.smu.endticket

import ac.kr.smu.endticket.auth.config.property.JWTProperties
import ac.kr.smu.endticket.common.redis.annotation.EnableAutoRedisConfig
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

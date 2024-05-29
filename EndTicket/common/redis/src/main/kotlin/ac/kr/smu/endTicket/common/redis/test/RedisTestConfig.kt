package ac.kr.smu.endTicket.common.redis.test

import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory

@TestConfiguration
class RedisTestConfig(
    @Value("\${spring.data.redis.host}")
    private val host: String,

    @Value("\${spring.data.redis.port}")
    private val port: Int
) {

    @Bean
    fun connectionFactory(): LettuceConnectionFactory = LettuceConnectionFactory(host, port)
}
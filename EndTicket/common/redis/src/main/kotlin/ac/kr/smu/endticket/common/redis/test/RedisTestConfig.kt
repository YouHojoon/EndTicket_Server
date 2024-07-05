package ac.kr.smu.endticket.common.redis.test

import org.springframework.boot.autoconfigure.data.redis.RedisProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory

/**
 * 테스트를 위한 Redis 설정
 * @property redisProperties Redis 설정
 */
@TestConfiguration
@EnableConfigurationProperties(RedisProperties::class)
class RedisTestConfig(
    val redisProperties: RedisProperties,
) {
    @Bean
    fun connectionFactory(): LettuceConnectionFactory = LettuceConnectionFactory(redisProperties.host, redisProperties.port)
}

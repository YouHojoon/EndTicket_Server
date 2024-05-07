package ac.kr.smu.endTicket.ticket.infra.config

import ac.kr.smu.endTicket.ticket.domain.model.Ticket
import ac.kr.smu.endTicket.ticket.infra.config.property.RedisClusterProperties
import io.lettuce.core.ReadFrom
import io.lettuce.core.cluster.ClusterClientOptions
import io.lettuce.core.cluster.ClusterTopologyRefreshOptions
import org.springframework.cache.CacheManager
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.cache.RedisCacheConfiguration
import org.springframework.data.redis.cache.RedisCacheManager
import org.springframework.data.redis.connection.RedisClusterConfiguration
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer
import org.springframework.data.redis.serializer.RedisSerializationContext
import org.springframework.data.redis.serializer.StringRedisSerializer
import java.time.Duration

@Configuration
class RedisConfig(
    private val clusterProperties: RedisClusterProperties
) {
    @Bean
    fun connectionFactory(): LettuceConnectionFactory{
        val topologyOption = ClusterTopologyRefreshOptions.builder()
            .enableAllAdaptiveRefreshTriggers()
            .build()
        val clientOption = ClusterClientOptions.builder()
            .topologyRefreshOptions(topologyOption)
            .build()

        val clientConfig = LettuceClientConfiguration
            .builder()
            .clientOptions(clientOption)
            .readFrom(ReadFrom.REPLICA)
            .build()

        val clusterConfig = RedisClusterConfiguration(clusterProperties.nodes)

        return LettuceConnectionFactory(clusterConfig, clientConfig)
    }
    @Bean
    fun redisTemplate(): RedisTemplate<String, Object> = RedisTemplate<String, Object>().apply {
        connectionFactory = connectionFactory()
        keySerializer = StringRedisSerializer()
        valueSerializer = GenericJackson2JsonRedisSerializer()
    }

    @Bean
    fun cacheManager(factory: RedisConnectionFactory): CacheManager{
        val config = RedisCacheConfiguration
            .defaultCacheConfig()
            .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(StringRedisSerializer()))
            .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(GenericJackson2JsonRedisSerializer()))
            .entryTtl(Duration.ofHours(2))

        return RedisCacheManager
            .RedisCacheManagerBuilder
            .fromConnectionFactory(factory)
            .cacheDefaults(config)
            .build()
    }
}
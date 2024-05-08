package ac.kr.smu.endTicket.config

import ac.kr.smu.endTicket.annotation.EnableAutoRedisConfig
import ac.kr.smu.endTicket.property.RedisClusterProperties
import io.lettuce.core.ReadFrom
import io.lettuce.core.cluster.ClusterClientOptions
import io.lettuce.core.cluster.ClusterTopologyRefreshOptions
import org.slf4j.LoggerFactory
import org.springframework.boot.actuate.autoconfigure.tracing.ConditionalOnEnabledTracing
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.cache.CacheManager
import org.springframework.cache.annotation.EnableCaching
import org.springframework.cloud.client.ConditionalOnDiscoveryEnabled
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
import org.springframework.data.redis.serializer.RedisSerializationContext
import org.springframework.data.redis.serializer.StringRedisSerializer
import java.time.Duration

@EnableCaching
@EnableConfigurationProperties(RedisClusterProperties::class)
class AutoRedisConfig(
    private val clusterProperties: RedisClusterProperties
) {
    private val log = LoggerFactory.getLogger(AutoRedisConfig::class.java)

    @Bean
    @ConditionalOnMissingBean(LettuceConnectionFactory::class)
    fun connectionFactory(): LettuceConnectionFactory {
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

        check(clusterProperties.nodes.isNotEmpty()){
            "클러스터의 노드가 없습니다."
        }

        val clusterConfig = RedisClusterConfiguration(clusterProperties.nodes)
        log.info("Redis Node: [${clusterProperties.nodes.joinToString(",")}] 들로 클러스터 설정 완료")

        return LettuceConnectionFactory(clusterConfig, clientConfig)
    }

    @Bean
    @ConditionalOnMissingBean(RedisTemplate::class)
    fun redisTemplate(connectionFactory: RedisConnectionFactory): RedisTemplate<String, Object> = RedisTemplate<String, Object>().apply {
        this.connectionFactory = connectionFactory
        keySerializer = StringRedisSerializer()
        valueSerializer = GenericJackson2JsonRedisSerializer()
    }

    @Bean
    @ConditionalOnMissingBean(CacheManager::class)
    fun cacheManager(factory: RedisConnectionFactory): CacheManager {
        val config = RedisCacheConfiguration
            .defaultCacheConfig()
            .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(StringRedisSerializer()))
            .serializeValuesWith(
                RedisSerializationContext.SerializationPair.fromSerializer(
                    GenericJackson2JsonRedisSerializer()
                )
            )
            .entryTtl(Duration.ofHours(2))

        return RedisCacheManager
            .RedisCacheManagerBuilder
            .fromConnectionFactory(factory)
            .cacheDefaults(config)
            .build()
    }

}
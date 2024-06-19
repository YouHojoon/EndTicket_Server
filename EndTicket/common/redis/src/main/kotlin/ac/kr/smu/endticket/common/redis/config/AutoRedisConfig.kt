package ac.kr.smu.endticket.common.redis.config

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule
import io.lettuce.core.ReadFrom
import io.lettuce.core.cluster.ClusterClientOptions
import io.lettuce.core.cluster.ClusterTopologyRefreshOptions
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.data.redis.RedisProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.cache.CacheManager
import org.springframework.cache.annotation.EnableCaching
import org.springframework.context.annotation.Bean
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

/**
 * Redis 클러스터의 자동 설정을 수행하는 클래스
 * 캐시 또한 Redis로 자동으로 설정한다.
 *
 * @param redisProperties Redis 설정 정보
 */
@EnableCaching
@EnableConfigurationProperties(RedisProperties::class)
class AutoRedisConfig(
    private val redisProperties: RedisProperties,
) {
    private val log = LoggerFactory.getLogger(AutoRedisConfig::class.java)

    /**
     * ValueSerializer를 등록하는 메소드, [GenericJackson2JsonRedisSerializer] 로 등록한다.
     * 모듈에 [ParameterNamesModule], [JavaTimeModule]을 자동으로 추가한다.
     */
    @Bean
    @ConditionalOnMissingBean(GenericJackson2JsonRedisSerializer::class)
    fun valueSerializer() = GenericJackson2JsonRedisSerializer().apply {
        configure {
            it.registerModule(ParameterNamesModule())
            it.registerModule(JavaTimeModule())
        }
    }

    /**
     * ConnectionFactory를 설정하는 메소드, 클러스터 토폴로지의 변화 감지를 설정하고 읽기 연산을 Replica에서 수행하도록 설정한다.
     * @throws IllegalStateException 설정에 등록되어 있는 클러스터 노드가 없을 때
     */
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

        check(redisProperties.cluster.nodes.isNotEmpty()){
            "클러스터의 노드가 없습니다."
        }

        val clusterConfig = RedisClusterConfiguration(redisProperties.cluster.nodes)
        log.info("Redis Node: [${redisProperties.cluster.nodes.joinToString(",")}] 들로 클러스터 설정 완료")

        return LettuceConnectionFactory(clusterConfig, clientConfig)
    }

    /**
     * [RedisTemplate]을 설정하는 메소드, key는 String으로 value는 JSON으로 직렬화한다.
     */
    @Bean
    @ConditionalOnClass(GenericJackson2JsonRedisSerializer::class)
    @ConditionalOnMissingBean(RedisTemplate::class)
    fun redisTemplate(connectionFactory: RedisConnectionFactory, valueSerializer: GenericJackson2JsonRedisSerializer) = RedisTemplate<String, Object>().apply {
        this.connectionFactory = connectionFactory
        keySerializer = StringRedisSerializer()
        this.valueSerializer = valueSerializer
    }

    /**
     * 캐시 매니저를 설정하는 메소드 key를 string, value를 JSON으로 직렬화하며
     * TTL을 두시간으로 설정한다.
     */
    @Bean
    @ConditionalOnClass(GenericJackson2JsonRedisSerializer::class)
    @ConditionalOnMissingBean(CacheManager::class)
    fun cacheManager(factory: RedisConnectionFactory, valueSerializer: GenericJackson2JsonRedisSerializer): CacheManager {
        val config = RedisCacheConfiguration
            .defaultCacheConfig()
            .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(StringRedisSerializer()))
            .serializeValuesWith(
                RedisSerializationContext.SerializationPair.fromSerializer(
                    valueSerializer
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
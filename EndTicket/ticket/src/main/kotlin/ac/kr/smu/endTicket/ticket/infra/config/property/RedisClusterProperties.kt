package ac.kr.smu.endTicket.ticket.infra.config.property

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("spring.data.redis.cluster")
data class RedisClusterProperties(
    val nodes: List<String>
)
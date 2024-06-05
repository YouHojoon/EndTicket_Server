package ac.kr.smu.endTicket.common.redis.annotation

import ac.kr.smu.endTicket.common.redis.config.AutoRedisConfig
import org.springframework.context.annotation.Import

/**
 * Redis 자동 설정을 허용하는 어노테이션
 * @see AutoRedisConfig
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@Import(AutoRedisConfig::class)
annotation class EnableAutoRedisConfig

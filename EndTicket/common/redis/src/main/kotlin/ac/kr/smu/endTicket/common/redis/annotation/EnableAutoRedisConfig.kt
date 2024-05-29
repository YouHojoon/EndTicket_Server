package ac.kr.smu.endTicket.common.redis.annotation

import ac.kr.smu.endTicket.common.redis.config.AutoRedisConfig
import org.springframework.context.annotation.Import

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@Import(AutoRedisConfig::class)
annotation class EnableAutoRedisConfig

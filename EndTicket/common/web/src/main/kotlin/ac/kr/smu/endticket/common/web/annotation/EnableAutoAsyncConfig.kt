package ac.kr.smu.endticket.common.web.annotation

import ac.kr.smu.endticket.common.web.config.AutoAsyncConfig
import org.springframework.context.annotation.Import

/**
 * 자동 비동기 설정을 활성화하는 어노테이션
 * @property corePoolSize [ThreadPoolTaskExecutor]의 corePoolSize
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@Import(AutoAsyncConfig::class)
annotation class EnableAutoAsyncConfig(
    val corePoolSize: Int = 3
)

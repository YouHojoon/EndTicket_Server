package ac.kr.smu.endTicket.common.web.annotation

import ac.kr.smu.endTicket.common.web.config.AutoAsyncConfig
import org.springframework.context.annotation.Import

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@Import(AutoAsyncConfig::class)
annotation class EnableAutoAsyncConfig(
    val corePoolSize: Int = 3
)

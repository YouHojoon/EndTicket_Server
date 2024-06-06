package ac.kr.smu.endTicket.common.web.config

import ac.kr.smu.endTicket.common.web.annotation.EnableAutoAsyncConfig
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean
import kotlin.reflect.full.findAnnotation


class AutoAsyncConfig(
    private val applicationContext: ApplicationContext
) {
    @Bean
    fun asyncConfig(): AsyncConfig {
        val beans = applicationContext.getBeansWithAnnotation(EnableAutoAsyncConfig::class.java)
        val corePoolSize = beans.mapNotNull { it.value::class.findAnnotation<EnableAutoAsyncConfig>() }.maxOf { it.corePoolSize }
        return AsyncConfig(corePoolSize)
    }
}
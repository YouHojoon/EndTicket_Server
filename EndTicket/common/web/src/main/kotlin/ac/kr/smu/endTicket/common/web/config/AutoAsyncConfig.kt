package ac.kr.smu.endTicket.common.web.config

import ac.kr.smu.endTicket.common.web.annotation.EnableAutoAsyncConfig
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.core.annotation.AnnotationUtils
import kotlin.reflect.full.findAnnotation


class AutoAsyncConfig(
    private val applicationContext: ApplicationContext
) {
    @Bean
    fun asyncConfig(): AsyncConfig {
        val beans = applicationContext.getBeansWithAnnotation(EnableAutoAsyncConfig::class.java)
        val corePoolSize = beans
            .mapNotNull { AnnotationUtils.findAnnotation(it.value.javaClass, EnableAutoAsyncConfig::class.java) }
            .maxOf { it.corePoolSize}
        
        return AsyncConfig(corePoolSize)
    }
}
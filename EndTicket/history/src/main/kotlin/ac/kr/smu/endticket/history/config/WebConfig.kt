package ac.kr.smu.endticket.history.config

import ac.kr.smu.endticket.history.domain.converter.HistoryTypeConverter
import org.springframework.context.annotation.Configuration
import org.springframework.format.FormatterRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
class WebConfig : WebMvcConfigurer {
    override fun addFormatters(registry: FormatterRegistry) {
        registry.addConverter(HistoryTypeConverter())
        super.addFormatters(registry)
    }
}

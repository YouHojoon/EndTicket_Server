package ac.kr.smu.endTicket.futureMe.infra.config

import ac.kr.smu.endTicket.futureMe.domain.converter.CharacterTypeConverter
import org.springframework.context.annotation.Configuration
import org.springframework.format.FormatterRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
class WebConfig: WebMvcConfigurer {
    override fun addFormatters(registry: FormatterRegistry) {
        registry.addConverter(CharacterTypeConverter())
        super.addFormatters(registry)
    }
}
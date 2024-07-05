package ac.kr.smu.endTicket.auth.config

import ac.kr.smu.endTicket.auth.domain.converter.SocialTypeConverter
import org.springframework.context.annotation.Configuration
import org.springframework.format.FormatterRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import java.util.*

@Configuration
class WebConfig : WebMvcConfigurer {
    override fun addFormatters(registry: FormatterRegistry) {
        registry.addConverter(SocialTypeConverter())
        super.addFormatters(registry)
    }
}

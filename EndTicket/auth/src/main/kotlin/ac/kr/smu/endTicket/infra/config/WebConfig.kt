package ac.kr.smu.endTicket.infra.config

import ac.kr.smu.endTicket.auth.domain.model.SocialType
import ac.kr.smu.endTicket.auth.ui.converter.SocialTypeConverter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.format.Formatter
import org.springframework.format.FormatterRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import java.util.*

@Configuration
class WebConfig : WebMvcConfigurer{
    override fun addFormatters(registry: FormatterRegistry) {
        registry.addConverter(SocialTypeConverter())
        super.addFormatters(registry)
    }
}
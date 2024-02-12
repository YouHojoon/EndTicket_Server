package ac.kr.smu.endTicket.infra.config

import ac.kr.smu.endTicket.auth.domain.model.SocialType
import feign.Retryer
import org.springframework.cloud.openfeign.EnableFeignClients
import org.springframework.cloud.openfeign.FeignFormatterRegistrar
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.format.FormatterRegistrar
import org.springframework.format.FormatterRegistry
import org.springframework.format.datetime.DateFormatterRegistrar
import java.util.concurrent.TimeUnit


@Configuration
@EnableFeignClients("ac.kr.smu.endTicket.infra.openfeign")
class OpenFeignConfig {
    @Bean
    fun retryer(): Retryer {
        return Retryer.Default(100L, TimeUnit.SECONDS.toMillis(3L), 3)
    }

    @Bean
    fun SocialTypeFeignFormatterRegistar(): FeignFormatterRegistrar{
        return  FeignFormatterRegistrar{registry:FormatterRegistry ->
            registry.addConverter(SocialType::class.java, String::class.java){
                it.name.lowercase()
            }
        }
    }
}
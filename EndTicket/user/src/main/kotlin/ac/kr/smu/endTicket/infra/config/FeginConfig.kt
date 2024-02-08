package ac.kr.smu.endTicket.infra.config

import feign.Retryer
import org.springframework.cloud.openfeign.EnableFeignClients
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.util.concurrent.TimeUnit

@Configuration
@EnableFeignClients("ac.kr.smu.endTicket.infra.client")
class FeginConfig {
    @Bean
    fun retryer(): Retryer{
        return Retryer.Default(100L, TimeUnit.SECONDS.toMillis(3L), 3)
    }
}
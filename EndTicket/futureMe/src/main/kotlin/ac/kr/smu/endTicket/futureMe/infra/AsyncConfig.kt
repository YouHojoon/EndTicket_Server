package ac.kr.smu.endTicket.futureMe.infra

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.EnableAsync
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor

@Configuration
@EnableAsync
class AsyncConfig{
    @Bean
    fun threadPoolTaskExecutor(): ThreadPoolTaskExecutor = ThreadPoolTaskExecutor().apply {
        // 참고 : https://medium.com/@greg.shiny82/트랜잭셔널-아웃박스-패턴의-실제-구현-사례-29cm-0f822fc23edb
        corePoolSize = 3 //파티션의 개수만큼 할당
        setAllowCoreThreadTimeOut(true)
        setWaitForTasksToCompleteOnShutdown(true)
        setAwaitTerminationSeconds(10)
    }
}
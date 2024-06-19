package ac.kr.smu.endticket.common.web.config

import org.springframework.context.annotation.Bean
import org.springframework.scheduling.annotation.EnableAsync
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor

/**
 * 비동기 자동 설정
 * @property [ThreadPoolTaskExecutor]의 corePoolSize
 */
@EnableAsync
class AutoAsyncConfig(
    private val corePoolSize: Int
){
    @Bean
    fun threadPoolTaskExecutor(): ThreadPoolTaskExecutor = ThreadPoolTaskExecutor().apply {
        // 참고 : https://medium.com/@greg.shiny82/트랜잭셔널-아웃박스-패턴의-실제-구현-사례-29cm-0f822fc23edb
        this.corePoolSize = this@AutoAsyncConfig.corePoolSize //파티션의 개수만큼 할당
        setAllowCoreThreadTimeOut(true)
        setWaitForTasksToCompleteOnShutdown(true)
        setAwaitTerminationSeconds(10)
    }
}
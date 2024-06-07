package ac.kr.smu.endTicket.futureMe.job

import ac.kr.smu.endTicket.futureMe.domain.event.repository.EventRepository
import ac.kr.smu.endTicket.futureMe.service.ImaginationCompletionEventMessageService
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.LocalDateTime
import kotlin.system.measureTimeMillis

@EnableScheduling
@Component
class ImaginationCompletionEventJob(
    private val repo: EventRepository,
    private val messageService: ImaginationCompletionEventMessageService
) {
    private val log = LoggerFactory.getLogger(ImaginationCompletionEventMessageService::class.java)

    @Scheduled(
        fixedDelayString = "\${schedules.resend-imagination-completion-event.fixedDelay}",
        initialDelayString = "\${schedules.resend-imagination-completion-event.initialDelay}"
    )
    fun resendImaginationCompletionEvent(){
        log.info("상상해보기 이벤트 재전송 시작")

        val elapsed = measureTimeMillis {
            val events = repo.findNotSentEventBefore(LocalDateTime.now().minusMinutes(10))

            messageService.sendMessages(events)
            repo.saveAll(events.filter { it.isSent })
        }

        log.info("상상해보기 이벤트 재전송 $elapsed ms 시간으로 완료")
    }
}
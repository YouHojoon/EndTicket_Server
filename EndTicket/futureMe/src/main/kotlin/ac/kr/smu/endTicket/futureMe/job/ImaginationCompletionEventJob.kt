package ac.kr.smu.endTicket.futureMe.job

import ac.kr.smu.endTicket.futureMe.domain.event.repository.EventRepository
import ac.kr.smu.endTicket.futureMe.infra.messaging.ImaginationCompletionEventMessageService
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.LocalDateTime
import java.util.concurrent.CompletableFuture
import kotlin.system.measureTimeMillis

/**
 * 미전송된 상상해보기 완료 이벤트를 전송하는 Job
 * @property repo 전송 완료를 저장하기 위한 저장소
 * @property messageService 메시지 전송을 하기 위한 서비스
 */
@EnableScheduling
@Component
class ImaginationCompletionEventJob(
    private val repo: EventRepository,
    private val messageService: ImaginationCompletionEventMessageService
) {
    private val log = LoggerFactory.getLogger(ImaginationCompletionEventMessageService::class.java)
    /**
     * 미전송된 이벤트를 재전송하는 메소드, 전송이 완료된 이벤트는 전송 완료를 저장한다.
     */
    @Scheduled(
        fixedDelayString = "\${schedules.resend-imagination-completion-event.fixedDelay}",
        initialDelayString = "\${schedules.resend-imagination-completion-event.initialDelay}"
    )
    fun resendImaginationCompletionEvent(){
        log.info("상상해보기 이벤트 재전송 시작")

        val elapsed = measureTimeMillis {
            val events = repo.findNotSentEventBefore(LocalDateTime.now().minusMinutes(10))
            val messages = events.map { it.toMessage() }
            val futures = messageService.sendMessages(messages).toTypedArray()

            val ids = CompletableFuture.allOf(*futures).thenApply {
                futures.mapIndexedNotNull { i, future ->
                    future.handle{result, e ->
                        val message = messages[i]

                        if (e == null)
                            result.producerRecord.value().id
                        else{
                            log.error("key: ${message.key}, payload: ${message.payload}", e)
                            null
                        }
                    }.get()
                }
            }.join()

            repo.saveAll(events.filter { it.eventID in ids }.map { it.also { it.successSend() } })
        }

        log.info("상상해보기 이벤트 재전송 $elapsed ms 시간으로 완료")
    }
}
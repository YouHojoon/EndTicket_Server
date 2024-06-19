package ac.kr.smu.endTicket.futureMe.job

import KafkaMessageService
import ac.kr.smu.endticket.common.kafka.constant.KafkaTopic
import ac.kr.smu.endTicket.futureMe.domain.event.repository.EventRepository
import ac.kr.smu.endTicket.futureMe.infra.messaging.ImaginationCompletedEventResponse
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
class ImaginationCompletedEventJob(
    private val repo: EventRepository,
    private val messageService: KafkaMessageService<String, ImaginationCompletedEventResponse>
) {
    private val log = LoggerFactory.getLogger(ImaginationCompletedEventJob::class.java)
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
            val futures = messageService
                .send(KafkaTopic.IMAGINATION_COMPLETION,messages)
                .mapIndexed {i, future ->
                    future.handle{record, e ->
                        if (e == null)
                            record.producerRecord.value().id
                        else {
                            val message = messages[i]
                            log.error("key: ${message.key}, payload: ${message.payload}",e)
                            null
                        }
                    }
                }
            CompletableFuture.allOf(*futures.toTypedArray()).join()
            val ids = futures.mapNotNull { it.join() }

            repo.saveAll(events.filter { it.imaginationID in ids }.map { it.also { it.successSend() } })
        }

        log.info("상상해보기 이벤트 재전송 $elapsed ms 시간으로 완료")
    }
}
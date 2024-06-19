package ac.kr.smu.endticket.ticket.job

import KafkaMessageService
import ac.kr.smu.endticket.common.kafka.constant.KafkaTopic
import ac.kr.smu.endticket.ticket.domain.repository.TicketCompletedEventRepository
import ac.kr.smu.endticket.ticket.ui.response.TicketResponse
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.LocalDateTime
import java.util.concurrent.CompletableFuture
import kotlin.system.measureTimeMillis

/**
 * [TicketCompletionEvent]의 관련된 일을 처리하는 Job
 * @property repo 이벤트를 저장하고 있는 저장소
 * @property messageService 메시지를 전송을 담당하는 서비스
 */
@Component
class TicketCompletedEventJob(
    private val repo: TicketCompletedEventRepository,
    private val messageService: KafkaMessageService<String, TicketResponse>
) {
    private val log = LoggerFactory.getLogger(TicketCompletedEventJob::class.java)

    /**
     * 발행된지 10분이 지났으나 메시지 전송이 되지 않은 이벤트를 재전송
     */
    @Scheduled(fixedDelayString = "\${schedules.resend-ticket-completion-event.fixedDelay}", initialDelayString = "\${schedules.resend-ticket-completion-event.initialDelay}")
    fun resendTicketCompletedEvent(){
        log.info("티켓 완료 이벤트 재전송 시작")

        val elapsed = measureTimeMillis {
            val events = repo.findByIsSentFalseAndAuditCreatedAtBefore(LocalDateTime.now().minusMinutes(10))
            val messages = events.map { it.toMessage() }
            val futures = messageService
                .send(KafkaTopic.TICKET_COMPLETION,messages)
                .mapIndexed { i, future ->
                    future.handle { record, e ->
                        if (e == null)
                            record.producerRecord.value().id
                        else {
                            val message = messages[i]
                            log.error("key: ${message.key}, payload: ${message.payload}", e)
                            null
                        }
                    }
                }

            CompletableFuture.allOf(*futures.toTypedArray()).join()
            val ids = futures.mapNotNull { it.join() }

            repo.saveAll(events.filter { it.id in ids }.map { it.also { it.successSend() } })
        }

        log.info("티켓 완료 이벤트 재전송 $elapsed ms 시간으로 완료")
    }
}
package ac.kr.smu.endTicket.ticket.domain.job

import ac.kr.smu.endTicket.ticket.domain.repository.TicketCompletionEventRepository
import ac.kr.smu.endTicket.ticket.service.TicketCompletionEventMessageService
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import kotlin.system.measureTimeMillis

/**
 * [TicketCompletionEvent]의 관련된 일을 처리하는 Job
 * @property repo TicketCompletionEvent를 저장하고 있는 저장소
 * @property messageService 메시지를 전송을 담당하는 서비스
 */
@Component
class TicketCompletionEventJob(
    private val repo: TicketCompletionEventRepository,
    private val messageService: TicketCompletionEventMessageService
) {
    private val log = LoggerFactory.getLogger(TicketCompletionEventJob::class.java)
    /**
     * 발행된지 10분이 지났으나 메시지 전송이 되지 않은 이벤트를 재전송
     */
    @Transactional
    @Scheduled(fixedDelayString = "\${schedules.resend-ticket-completion-event.fixedDelay}", initialDelayString = "\${schedules.resend-ticket-completion-event.initialDelay}")
    fun resendTicketCompletionEvent(){
        log.info("티켓 완료 이벤트 재전송 시작")

        val elapsed = measureTimeMillis {
            repo
                .findByIsSentFalseAndAuditCreatedAtBefore(LocalDateTime.now().minusMinutes(10))
                .forEach {
                    messageService.send(it)
                }
        }

        log.info("티켓 완료 이벤트 재전송 $elapsed ms 시간으로 완료")
    }
}
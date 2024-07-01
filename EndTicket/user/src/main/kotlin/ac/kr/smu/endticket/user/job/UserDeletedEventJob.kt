package ac.kr.smu.endticket.user.job

import KafkaMessageService
import ac.kr.smu.endticket.common.kafka.constant.KafkaTopic
import ac.kr.smu.endticket.common.kafka.messaging.KafkaMessage
import ac.kr.smu.endticket.user.domain.repository.UserDeletedEventRepository
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.concurrent.CompletableFuture
import kotlin.system.measureTimeMillis

@Component
class UserDeletedEventJob(
    private val repo: UserDeletedEventRepository,
    private val messageService: KafkaMessageService<String, Void>
) {
    private val log = LoggerFactory.getLogger(UserDeletedEventJob::class.java)

    @Scheduled(fixedDelayString = "\${schedules.resend-user-deleted-event.fixed-delay}", initialDelayString = "\${schedules.resend-user-deleted-event.inital-delay}")
    @Transactional
    fun resend(){
        log.info("사용자 탈퇴 이벤트 재전송 시작")

        val elapsed = measureTimeMillis {
            val events = repo
                .findByAuditCreatedAtBefore(LocalDateTime.now().minusMinutes(10))
            val messages = events.map {it.toMessage()}
            val futures = messageService
                .send(KafkaTopic.USER_DELETED, messages)
                .mapIndexed{i,future ->
                    future.handle{record, e ->
                        if (e == null)
                            record.producerRecord.key().toLong()
                        else {
                            log.error("{key: ${messages[i].key}}", e)
                            null
                        }
                    }
                }

            CompletableFuture.allOf(*futures.toTypedArray()).join()
            repo.deleteAllById(futures.mapNotNull { it.join() })
        }

        log.info("사용자 탈퇴 이벤트 재전송 $elapsed ms 시간으로 완료")
    }
}
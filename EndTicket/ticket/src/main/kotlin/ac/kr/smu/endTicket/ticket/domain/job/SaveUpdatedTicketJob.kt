package ac.kr.smu.endTicket.ticket.domain.job

import ac.kr.smu.endTicket.redis.getKeysWithPattern
import ac.kr.smu.endTicket.ticket.domain.model.Ticket
import ac.kr.smu.endTicket.ticket.domain.repository.TicketRepository
import org.slf4j.LoggerFactory
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import kotlin.system.measureTimeMillis

/**
 * [Ticket] 캐시의 내용을 저장하는 Job
 * @property redisTemplate Redis를 접근하기 위한 객체
 * @property repo Ticket을 저장하고 있는 저장소
 */
@Component
class SaveUpdatedTicketJob(
    private val redisTemplate: RedisTemplate<String, Any>,
    private val repo: TicketRepository
) {
    private companion object{
        private const val REDIS_KEY_PREFIX = "ticket::"
    }
    private val log = LoggerFactory.getLogger(SaveUpdatedTicketJob::class.java)

    /**
     * 캐시의 내용을 DB에 저장하는 메소드
     */
    @Scheduled(initialDelayString = "\${schedules.save-updated-ticket.initialDelay}",fixedDelayString = "\${schedules.save-updated-ticket.fixedDelay}")
    @Transactional
    fun saveUpdatedTicketToDB(){
        log.info("캐시 DB로 업데이트 작업 시작")

        val elapsed = measureTimeMillis {
            val keys = redisTemplate
                .getKeysWithPattern("${REDIS_KEY_PREFIX}*")
            val ops = redisTemplate.opsForValue()

            val ticketsOfCache = keys
                .mapNotNull { ops.get(it) as? Ticket }
                .filter { it.shouldUpdate }

            repo.saveAll(ticketsOfCache)

            ticketsOfCache
                .forEach {
                    it.updateComplete()
                    ops.setIfPresent("${REDIS_KEY_PREFIX}${it.id}", it)
                }
        }
        log.info("캐시 DB로 업데이트 작업 $elapsed ms의 시간으로 완료")
    }
}
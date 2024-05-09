package ac.kr.smu.endTicket.ticket.service

import ac.kr.smu.endTicket.ticket.domain.exception.NotFoundTicketException
import ac.kr.smu.endTicket.ticket.domain.exception.NotOwnerOfTicketException
import ac.kr.smu.endTicket.ticket.domain.model.Ticket
import ac.kr.smu.endTicket.ticket.domain.repository.TicketRepository
import ac.kr.smu.endTicket.ticket.ui.request.TicketRequest
import org.slf4j.LoggerFactory
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.CachePut
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.core.ScanOptions
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import kotlin.jvm.optionals.getOrNull
import kotlin.system.measureTimeMillis

/**
 * 티켓 관련한 기능을 처리하는 클래스
 */
@Service
class TicketService(
    private val repo: TicketRepository,
    private val redisTemplate: RedisTemplate<String, Any>
) {
    private val log = LoggerFactory.getLogger(TicketService::class.java)
    private companion object{
        private const val REDIS_KEY_PREFIX = "ticket::"
    }

    /**
     * 티켓을 생성하는 메소드
     * @param request 티켓 생성에 대한 요청
     * @param userID 티켓 생성을 요청한 user의 ID
     * @return 생성된 티켓
     */
    @CachePut(cacheNames = ["ticket"], key = "#result.id")
    @Transactional
    fun createTicket(request: TicketRequest, userID: Long): Ticket{
        return repo.save(Ticket(request,userID))
    }

    /**
     * 티켓을 수정하는 메소드
     * @param request 수정할 티켓 요청
     * @param id 티켓 id
     * @return 수정된 티켓
     * @throws NotFoundTicketException id로 조회한 티켓이 없을 시
     */

    @Throws(NotFoundTicketException::class)
    @CachePut(cacheNames = ["ticket"], key = "#id")
    @Transactional
    fun updateTicket(request: TicketRequest, id: Long, userID: Long): Ticket{
        val old = repo.findById(id).getOrNull() ?: throw NotFoundTicketException(id)

        old.update(request,userID)

        return old
    }

    /**
     * 티켓 스와이프를 처리하는 메소드, 관련 결과는 캐시된다.
     * @param 티켓의 id
     * @return 스와이프 결과가 반영된 티켓
     * @throws NotFoundTicketException id로 조회한 티켓이 없을 시
     */
    @CachePut(cacheNames = ["ticket"], key = "#id")
    @Transactional
    fun swipeTicket(id: Long, userID: Long): Ticket{
        val ticket = repo.findById(id).getOrNull() ?: throw NotFoundTicketException(id)

        if (ticket.swipeAndCheckCompletion(userID))
            completeTicket(id)

        return ticket
    }

    /**
     * 캐시의 내용을 DB에 저장하는 메소드
     */
    @Scheduled(initialDelayString = "\${schedules.save-updatedTicket-toDB.initialDelay}",fixedDelayString = "\${schedules.save-updatedTicket-toDB.fixedDelay}")
    @Transactional
    fun saveUpdatedTicketToDB(){
        log.info("캐시 DB로 업데이트 작업 시작")

        val elapsed = measureTimeMillis {
            val keys = redisTemplate.getKeysWithPattern("${REDIS_KEY_PREFIX}*")
            val ops = redisTemplate.opsForValue()

            val ticketsOfCache = keys
                .map { ops.get(it) as Ticket }
                .filter { it.shouldUpdate }

            repo.saveAll(ticketsOfCache)

            ticketsOfCache
                .map { it.apply { shouldUpdate = false } }
                .forEach { ops.setIfPresent("$REDIS_KEY_PREFIX${it.id}", it) }
        }


        log.info("캐시 DB로 업데이트 작업 $elapsed ms의 시간으로 완료")
    }

    @Transactional
    @CacheEvict("ticket", key = "#id")
    fun completeTicket(id:Long){
        repo.deleteById(id)
    }

    /**
     * scan을 통해 패턴에 맞는 키를 가져오는 메소드
     * @param pattern 키의 패턴
     * @param count scan의 카운트, 기본값은 200
     * @return 조건에 맞는 키의 set
     */
    private fun RedisTemplate<String, Any>.getKeysWithPattern(pattern: String, count: Long = 200): Set<String>{
        val keys = HashSet<String>()
        execute{
            try {
                scan(ScanOptions.scanOptions().match(pattern).count(count).build()).use {
                    while (it.hasNext())
                        keys.add(it.next())
                }
            }catch (e: Exception) {
                throw e
            }
        }
        return keys
    }


}
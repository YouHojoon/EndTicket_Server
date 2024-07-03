package ac.kr.smu.endticket.history.service

import ac.kr.smu.endticket.history.domain.model.History
import ac.kr.smu.endticket.history.domain.model.ImaginationHistory
import ac.kr.smu.endticket.history.domain.model.TicketHistory
import ac.kr.smu.endticket.history.ui.response.HistorySlice
import ac.kr.smu.endticket.history.domain.repository.HistoryRepository
import ac.kr.smu.endticket.history.infra.messaging.EventResponse
import ac.kr.smu.endticket.history.infra.messaging.ImaginationCompletedEventResponse
import ac.kr.smu.endticket.history.infra.messaging.TicketCompletedEventResponse
import ac.kr.smu.endticket.history.ui.response.HistoryCount
import ac.kr.smu.endticket.history.ui.response.HistoryResponse
import org.springframework.data.domain.Pageable
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class HistoryService(
    private val repo: HistoryRepository,
    private val redisTemplate: RedisTemplate<String, Any>
) {
    /**
     * 사용자의 기록들을 조회하는 메소드
     * @param userId 사용자 id
     * @param type 조회할 기록 종류
     * @param pageable 조회할 페잊
     * @return 조회된 기록들
     */
    @Transactional(readOnly = true)
    fun findHistories(userId: Long, type: History.Type, pageable: Pageable): HistorySlice<HistoryResponse> =
        repo.findAllByUserIdAndType(userId, type, pageable).map(History::toResponse)
    /**
     * 사용자 기록의 각각의 개수를 반환하는 메소드
     * @param userId
     * @return 기록의 개수들
     */
    @Transactional(readOnly = true)
    fun findHistoryCount(userId: Long): HistoryCount = repo.countEachHistoryByUserId(userId)

    /**
     * 기록을 저장하는 메소드, 사용자 기록 개수가 캐시에 있다면 이를 업데이트한다. 만약 이미 저장되어 있다면 아무것도 하지 않는다.
     * @param history 저장할 기록
     */
    @Transactional
    fun saveHistory(eventResponse: EventResponse, userId: Long){
        val (type, history) = when(eventResponse){
            is TicketCompletedEventResponse -> History.Type.TICKET to TicketHistory.from(eventResponse, userId)
            is ImaginationCompletedEventResponse -> History.Type.IMAGINATION to ImaginationHistory.from(eventResponse, userId)
        }

        if (repo.existsBySpecificIdAndType(eventResponse.id, type))
            return

        repo.save(history)
        redisTemplate.updateCountIfPresent(history)
    }


    /**
     * 기록 개수가 캐시에 있으면 업데이트 하는 메소드
     * @param userId 사용자 id
     * @param type 새로 저장된 기록 종류
     * @throws IllegalArgumentException 기록의 타입이 지원하지 않는 타입일 때
     */
    private fun RedisTemplate<String,Any>.updateCountIfPresent(history: History){
        val ops = opsForValue()
        val key = "history-count::${history.userId}"
        val count = ops.get(key) as? HistoryCount ?: return

        val newCount = when(history){
            is TicketHistory -> HistoryCount(count.ticketHistoryCount + 1, count.ticketSwipeCount + history.swipeCount, count.imaginationHistoryCount)
            is ImaginationHistory -> HistoryCount(count.ticketHistoryCount,count.ticketSwipeCount,count.imaginationHistoryCount + 1)
            else -> throw IllegalArgumentException("$${history::class}는 지원하지 않는 타입입니다.")
        }

        ops.set(key,newCount)
    }
}
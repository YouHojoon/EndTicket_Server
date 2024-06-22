package ac.kr.smu.endticket.history.domain.repository

import ac.kr.smu.endticket.history.domain.model.History
import ac.kr.smu.endticket.history.ui.response.HistoryCount
import ac.kr.smu.endticket.history.ui.response.HistorySlice
import org.springframework.data.domain.Pageable

interface HistorySupport {
    /**
     * type에 맞는 기록 중 특정 id를 가진 기록을 반환하는 메소드
     * @param specificId id
     * @param type 찾을 기록 종류
     * @return 기록, 없다면 null
     */
    fun findBySpecificIdAndType(specificId: Long, type: History.Type): History?
    /**
     * type에 맞는 기록 중 특정 id를 가진 기록의 존재 여부를 반환하는 메소드
     * @param specificId id
     * @param type 찾을 기록 종류
     * @return 기록의 존재여부
     */
    fun existsBySpecificIdAndType(specificId: Long, type: History.Type): Boolean

    /**
     * 사용자의 기록들을 반환하는 메소드
     * @param userId 사용자 id
     * @param type 찾을 기록 종류
     * @param pageable 페이지
     * @return 조회된 기록 들
     */
    fun findAllByUserIdAndType(userId: Long, type: History.Type, pageable: Pageable): HistorySlice<History>

    /**
     * 사용자 기록들 각각의 개수를 반환하는 메소드
     * @param userId
     * @return 기록의 개수들
     */
    fun countEachHistoryByUserId(userId: Long): HistoryCount


}
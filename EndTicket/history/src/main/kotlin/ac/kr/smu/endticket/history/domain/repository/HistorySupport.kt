package ac.kr.smu.endticket.history.domain.repository

import ac.kr.smu.endticket.history.domain.model.History
import kotlin.reflect.KClass

interface HistorySupport {
    /**
     * type에 맞는 기록 중 특정 id를 가진 기록을 반환하는 메소드
     * @param specificId id
     * @param type 찾을 기록 종류
     * @return 기록, 없다면 null
     */
    fun findBySpecificIdAndType(specificId: Long, type: KClass<out History>): History?
    /**
     * type에 맞는 기록 중 특정 id를 가진 기록의 존재 여부를 반환하는 메소드
     * @param specificId id
     * @param type 찾을 기록 종류
     * @return 기록의 존재여부
     */
    fun existsBySpecificIdAndType(specificId: Long, type: KClass<out History>): Boolean
}
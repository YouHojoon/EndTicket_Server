package ac.kr.smu.endTicket.futureMe.domain.event.repository

import ac.kr.smu.endTicket.futureMe.domain.event.model.Event
import kotlin.reflect.KClass

interface EventRepositorySupport {
    /**
     * 이벤트의 존재 여부를 조회하는 메소드
     * @param specificID 이벤트의 ID
     * @param type 이벤트의 종류
     * @return 존재 여부
     */
    fun existsBySpecificIDAndType(specificID: Long, type: KClass<out Event>): Boolean
}
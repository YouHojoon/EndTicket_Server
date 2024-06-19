package ac.kr.smu.endticket.futureme.domain.event.repository

import ac.kr.smu.endticket.futureme.domain.event.model.Event
import kotlin.reflect.KClass

interface EventRepositorySupport {
    /**
     * 이벤트의 존재 여부를 조회하는 메소드
     * @param specificId 이벤트의 Id
     * @param type 이벤트의 종류
     * @return 존재 여부
     */
    fun existsBySpecificIdAndType(specificId: Long, type: KClass<out Event>): Boolean
}
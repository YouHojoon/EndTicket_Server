package ac.kr.smu.endticket.history.domain.repository

import ac.kr.smu.endticket.history.domain.model.History
import kotlin.reflect.KClass

interface HistorySupport {
    fun findBySpecificIdAndType(specificId: Long, type: KClass<out History>): History?
    fun existsBySpecificIdAndType(specificId: Long, type: KClass<out History>): Boolean
}
package ac.kr.smu.endticket.history.domain.repository

import ac.kr.smu.endticket.history.domain.model.History
import ac.kr.smu.endticket.history.domain.model.ImaginationHistory
import ac.kr.smu.endticket.history.domain.model.TicketHistory
import jakarta.persistence.EntityManager
import org.springframework.stereotype.Repository
import kotlin.reflect.KClass

@Repository
class HistorySupportImpl(
    private val em: EntityManager
): HistorySupport {

    override fun findBySpecificIdAndType(specificId: Long, type: KClass<out History>): History? {
        val (table, spec) = tableAndSpecOfType(type)

        val query = """
            SELECT * FROM $table WHER $spec = :specificId
        """.trimIndent()

        return em.createNativeQuery(query, History::class.java)
            .setParameter("specificId", specificId)
            .resultList.first() as? History
    }

    override fun existsBySpecificIdAndType(specificId: Long, type: KClass<out History>): Boolean {
        val (table, spec) = tableAndSpecOfType(type)
        val query = """
            SELECT EXISTS(SELECT 1 FROM $table WHER $spec = :specificId)
        """.trimIndent()

        return (
                em.createNativeQuery(query, History::class.java)
                    .setParameter("specificId", specificId)
                    .singleResult as Number
                ).toInt() == 1
    }

    private fun tableAndSpecOfType(type: KClass<out History>) = when(type){
        TicketHistory::class -> "ticket_history" to "ticket_id"
        ImaginationHistory::class -> "imagination_history" to "imagination_id"
        else -> throw IllegalArgumentException("$type 은 지원하지 않는 이벤트 타입입니다.")
    }
}
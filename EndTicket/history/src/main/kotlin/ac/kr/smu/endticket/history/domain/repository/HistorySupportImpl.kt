package ac.kr.smu.endticket.history.domain.repository

import ac.kr.smu.endticket.history.domain.model.History
import ac.kr.smu.endticket.history.domain.model.HistorySlice
import ac.kr.smu.endticket.history.domain.model.ImaginationHistory
import ac.kr.smu.endticket.history.domain.model.TicketHistory
import jakarta.persistence.EntityManager
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Slice
import org.springframework.data.domain.SliceImpl
import org.springframework.stereotype.Repository
import kotlin.reflect.KClass
import kotlin.reflect.cast
import kotlin.reflect.safeCast

@Repository
class HistorySupportImpl(
    private val em: EntityManager
): HistorySupport {

    override fun findBySpecificIdAndType(specificId: Long, type: History.Type): History? {
        val (table, spec) = tableAndSpecOfType(type)

        val query = """
            SELECT * FROM history as h JOIN $table as st 
            WHERE st.$spec = :specificId and h.id = st.id
        """.trimIndent()

        return em.createNativeQuery(query, type.toHistoryClass().java)
            .setParameter("specificId", specificId)
            .resultList.firstOrNull() as? History

    }

    override fun existsBySpecificIdAndType(specificId: Long, type: History.Type): Boolean {
        val (table, spec) = tableAndSpecOfType(type)
        val query = """
            SELECT EXISTS(SELECT 1 FROM $table WHERE $spec = :specificId)
        """.trimIndent()

        return (
                em.createNativeQuery(query)
                    .setParameter("specificId", specificId)
                    .singleResult as Number
                ).toInt() == 1
    }

    override fun findAllByUserIdAndType(userId: Long, type: History.Type, pageable: Pageable): HistorySlice<History> {
        val table = when(type){
            History.Type.TICKET -> "ticket_history"
            History.Type.IMAGINATION -> "imagination_history"
        }

        val sortOrder = pageable.sort.joinToString(", "){
            "${it.property} ${it.direction.name}"
        }

        val query = """
            SELECT * FROM history as h JOIN $table as sb  WHERE h.id = sb.id AND h.user_id = :userId
            ORDER BY ${sortOrder.ifEmpty { "h.completed_at DESC" }} LIMIT :size OFFSET :offset
        """.trimIndent()

        val result = em.createNativeQuery(query, type.toHistoryClass().java)
            .setParameter("userId",userId)
            .setParameter("size",pageable.pageSize + 1)
            .setParameter("offset", pageable.offset)
            .resultList
            .map { type.toHistoryClass().cast(it) }

        return HistorySlice(result, pageable)
    }

    /**
     * type에 맞는 table 이름과 specificId와 매칭시킬 column 명을 반환하는 메소드
     * @param type 기록 종류
     * @return table 이름과 column 명
     */
    private fun tableAndSpecOfType(type: History.Type) = when(type){
        History.Type.TICKET -> "ticket_history" to "ticket_id"
        History.Type.IMAGINATION -> "imagination_history" to "imagination_id"
    }
}
package ac.kr.smu.endticket.history.domain.repository

import ac.kr.smu.endticket.history.domain.model.History
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

@Repository
class HistorySupportImpl(
    private val em: EntityManager
): HistorySupport {

    override fun findBySpecificIdAndType(specificId: Long, type: KClass<out History>): History? {
        val (table, spec) = tableAndSpecOfType(type)

        val query = """
            SELECT * FROM history as h JOIN $table as st WHERE st.$spec = :specificId and h.id = st.id
        """.trimIndent()

        return em.createNativeQuery(query, type.java)
            .setParameter("specificId", specificId)
            .resultList.firstOrNull() as? History
    }

    override fun existsBySpecificIdAndType(specificId: Long, type: KClass<out History>): Boolean {
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

    override fun findAllByUserIdAndType(userId: Long, type: KClass<out History>, pageable: Pageable): Slice<out History> {
        val table = when(type){
            TicketHistory::class -> "ticket_history"
            ImaginationHistory::class -> "imagination_history"
            else -> throw IllegalArgumentException("$type 은 지원하지 않는 이벤트 타입입니다.")
        }

        val query = """
            SELECT * FROM history as h JOIN $table as sb  WHERE h.id = sb.id AND h.user_id = :userId
            ORDER BY h.completed_at DESC LIMIT :size OFFSET :offset
        """.trimIndent()

        val result = em.createNativeQuery(query, type.java)
            .setParameter("userId",userId)
            .setParameter("size",pageable.pageSize + 1)
            .setParameter("offset", pageable.offset)
            .resultList
            .map { type.cast(it) }

        return SliceImpl(result.dropLast(1),pageable,result.size == pageable.pageSize + 1)
    }

    /**
     * type에 맞는 table 이름과 specificId와 매칭시킬 column 명을 반환하는 메소드
     * @param type 기록 종류
     * @return table 이름과 column 명
     */
    private fun tableAndSpecOfType(type: KClass<out History>) = when(type){
        TicketHistory::class -> "ticket_history" to "ticket_id"
        ImaginationHistory::class -> "imagination_history" to "imagination_id"
        else -> throw IllegalArgumentException("$type 은 지원하지 않는 이벤트 타입입니다.")
    }
}
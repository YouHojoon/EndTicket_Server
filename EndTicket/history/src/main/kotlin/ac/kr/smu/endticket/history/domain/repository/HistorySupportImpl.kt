package ac.kr.smu.endticket.history.domain.repository

import ac.kr.smu.endticket.history.constant.RedisConstant
import ac.kr.smu.endticket.history.domain.model.History
import ac.kr.smu.endticket.history.ui.response.HistoryCount
import ac.kr.smu.endticket.history.ui.response.HistorySlice
import jakarta.persistence.EntityManager
import org.hibernate.query.NativeQuery
import org.springframework.cache.annotation.Cacheable
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import kotlin.reflect.cast

@Repository
class HistorySupportImpl(
    private val em: EntityManager,
) : HistorySupport {
    @Transactional(readOnly = true)
    override fun findBySpecificIdAndType(
        specificId: Long,
        type: History.Type,
    ): History? {
        val (table, spec) = tableAndSpecOfType(type)

        val query =
            """
            SELECT * FROM history as h JOIN $table as st 
            WHERE st.$spec = :specificId and h.id = st.id
            """.trimIndent()

        return em
            .createNativeQuery(query, type.toHistoryClass().java)
            .setParameter("specificId", specificId)
            .resultList
            .firstOrNull() as? History
    }

    @Transactional(readOnly = true)
    override fun existsBySpecificIdAndType(
        specificId: Long,
        type: History.Type,
    ): Boolean {
        val (table, spec) = tableAndSpecOfType(type)
        val query =
            """
            SELECT EXISTS(SELECT 1 FROM $table WHERE $spec = :specificId)
            """.trimIndent()

        return (
            em
                .createNativeQuery(query)
                .setParameter("specificId", specificId)
                .singleResult as Number
        ).toInt() == 1
    }

    @Transactional(readOnly = true)
    override fun findAllByUserIdAndType(
        userId: Long,
        type: History.Type,
        pageable: Pageable,
    ): HistorySlice<History> {
        val table =
            when (type) {
                History.Type.TICKET -> "ticket_history"
                History.Type.IMAGINATION -> "imagination_history"
            }

        val sortOrder =
            pageable.sort.joinToString(", ") {
                "${it.property} ${it.direction.name}"
            }

        val query =
            """
            SELECT * FROM history as h JOIN $table as sb  WHERE h.id = sb.id AND h.user_id = :userId
            ORDER BY ${sortOrder.ifEmpty { "h.completed_at DESC" }} LIMIT :size OFFSET :offset
            """.trimIndent()

        val result =
            em
                .createNativeQuery(query, type.toHistoryClass().java)
                .setParameter("userId", userId)
                .setParameter("size", pageable.pageSize + 1)
                .setParameter("offset", pageable.offset)
                .resultList
                .map { type.toHistoryClass().cast(it) }

        return HistorySlice(result, pageable)
    }

    @Transactional(readOnly = true)
    @Cacheable(cacheNames = [RedisConstant.HISTORY_COUNT_REDIS_PREFIX], key = "#userId")
    override fun countEachHistoryByUserId(userId: Long): HistoryCount {
        val query =
            """
            SELECT
                COUNT(th.id) AS ticket_history_count,
                COUNT(ih.id) AS imagination_history_count,
                SUM(th.swipe_count) AS ticket_swipe_count
            FROM history h
            LEFT JOIN ticket_history th ON th.id = h.id
            LEFT JOIN imagination_history ih ON ih.id = h.id
            WHERE h.user_id = :userId
            """.trimIndent()

        return (
            em
                .createNativeQuery(query)
                .setParameter("userId", userId)
                .unwrap(NativeQuery::class.java)
                .addScalar("ticket_history_count", Int::class.java)
                .addScalar("imagination_history_count", Int::class.java)
                .addScalar("ticket_swipe_count", Int::class.java)
                .setTupleTransformer { tuple, _ ->
                    HistoryCount(
                        ticketHistoryCount = tuple[0] as Int,
                        imaginationHistoryCount = tuple[1] as Int,
                        ticketSwipeCount = tuple.last() as Int,
                    )
                }.singleResultOrNull
        ) ?: HistoryCount(0, 0, 0)
    }

    private fun table(type: History.Type) =
        when (type) {
            History.Type.TICKET -> "ticket_history"
            History.Type.IMAGINATION -> "imagination_history"
        }

    /**
     * type에 맞는 table 이름과 specificId와 매칭시킬 column 명을 반환하는 메소드
     * @param type 기록 종류
     * @return table 이름과 column 명
     */
    private fun tableAndSpecOfType(type: History.Type) =
        when (type) {
            History.Type.TICKET -> table(type) to "ticket_id"
            History.Type.IMAGINATION -> table(type) to "imagination_id"
        }
}

package ac.kr.smu.endticket.history.ui.response

import com.fasterxml.jackson.annotation.JsonIgnore
import io.swagger.v3.oas.annotations.media.ArraySchema
import io.swagger.v3.oas.annotations.media.Schema
import org.springframework.data.domain.Pageable
import java.util.*
import java.util.function.Consumer

/**
 * 기록들과 마지막 페이지 여부를 가지는 클래스
 * @property histories 기록들
 * @property pageable 조회에 사용된 paegable
 */

@Schema(description = "기록들과 마지막 페이지 여부를 나타내는 클래스")
data class HistorySlice<T>(


    @Schema(type = "array", oneOf = [TicketHistoryResponse::class, ImaginationHistoryResponse::class])
    val histories: Collection<T>,
    @Schema(hidden = true)
    private val pageable: Pageable
): Iterable<T>{

    /**
     * 마지막 페이지 여부
     */
    @Schema(description = "마지막 페이지 여부", example = "true")
    val last: Boolean = histories.size < pageable.pageSize
    @Schema(hidden = true)
    fun <R> map(transform: (T) -> R): HistorySlice<R> {
        return HistorySlice(histories.map(transform), pageable)
    }
    @Schema(hidden = true)
    override fun forEach(action: Consumer<in T>?) = histories.forEach(action)
    @Schema(hidden = true)
    override fun iterator(): Iterator<T> = histories.iterator()
    @Schema(hidden = true)
    override fun spliterator(): Spliterator<T> = histories.spliterator()
    @Schema(hidden = true)
    fun isEmpty() = histories.isEmpty()
    @Schema(hidden = true)
    fun isNotEmpty() = !isEmpty()
}

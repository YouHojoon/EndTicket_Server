package ac.kr.smu.endticket.history.domain.model

import org.springframework.data.domain.Pageable
import java.util.*
import java.util.function.Consumer

data class HistorySlice<T>(
    val histories: Collection<T>,
    private val pageable: Pageable
): Iterable<T>{
    val last: Boolean

    init {
        last = histories.size < pageable.pageSize
    }

    fun <R> map(transform: (T) -> R): HistorySlice<R>{
        return HistorySlice(histories.map(transform), pageable)
    }

    override fun forEach(action: Consumer<in T>?) = histories.forEach(action)

    override fun iterator(): Iterator<T> = histories.iterator()

    override fun spliterator(): Spliterator<T> = histories.spliterator()

    fun isEmpty() = histories.isEmpty()
    fun isNotEmpty() = histories.isNotEmpty()
}

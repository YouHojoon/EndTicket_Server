package ac.kr.smu.endTicket.test

import org.jetbrains.annotations.TestOnly
import org.mockito.Mockito

@Suppress("UNCHECKED_CAST")
@TestOnly
fun <T> mockAny(): T{
    Mockito.any<T>()
    return null as T
}
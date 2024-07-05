package ac.kr.smu.endticket.futureme.futureme

import ac.kr.smu.endticket.common.web.enum.CharacterType
import ac.kr.smu.endticket.futureme.domain.event.model.ImaginationCompletedEvent
import ac.kr.smu.endticket.futureme.domain.event.model.TicketCompletedEvent
import ac.kr.smu.endticket.futureme.domain.futureme.model.Character
import ac.kr.smu.endticket.futureme.domain.futureme.model.FutureMe
import ac.kr.smu.endticket.futureme.ui.request.CreateFutureMeRequest
import ac.kr.smu.endticket.futureme.ui.request.UpdateFutureMeRequest
import org.junit.jupiter.params.provider.Arguments
import org.mockito.Mockito
import java.util.stream.Stream

object FutureMeTestParameters {
    const val PATH = "ac.kr.smu.endticket.futureme.futureme.FutureMeTestParameters"
    const val USER_ID = 1L
    val UPDATE_REQUEST = UpdateFutureMeRequest("title", CharacterType.VEGA)

    @JvmStatic
    fun provideFutureMeAndEvent() =
        Stream.of(
            Arguments.of(
                FutureMe.from(CreateFutureMeRequest(CharacterType.CHEESE), USER_ID),
                Mockito.mock(TicketCompletedEvent::class.java).also { Mockito.`when`(it.userId).thenReturn(USER_ID) },
                20,
            ),
            Arguments.of(
                FutureMe.from(CreateFutureMeRequest(CharacterType.CHEESE), USER_ID),
                Mockito.mock(ImaginationCompletedEvent::class.java).also { Mockito.`when`(it.userId).thenReturn(USER_ID) },
                10,
            ),
        )

    @JvmStatic
    fun provideCharacterAndEvent() =
        Stream.of(
            Arguments.of(
                Character(CharacterType.VEGA),
                Mockito.mock(TicketCompletedEvent::class.java),
                20,
            ),
            Arguments.of(
                Character(CharacterType.VEGA),
                Mockito.mock(ImaginationCompletedEvent::class.java),
                10,
            ),
        )
}

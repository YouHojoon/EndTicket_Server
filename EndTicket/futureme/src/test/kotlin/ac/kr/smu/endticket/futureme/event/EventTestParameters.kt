package ac.kr.smu.endticket.futureme.event

import ac.kr.smu.endticket.common.web.enum.CharacterType
import ac.kr.smu.endticket.futureme.domain.event.model.ImaginationCompletedEvent
import ac.kr.smu.endticket.futureme.domain.futureme.model.FutureMe
import ac.kr.smu.endticket.futureme.domain.imagination.model.Imagination
import ac.kr.smu.endticket.futureme.imagination.ImaginationParameters
import ac.kr.smu.endticket.futureme.infra.messaging.TicketCompletedEventResponse
import ac.kr.smu.endticket.futureme.ui.request.CreateFutureMeRequest
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.junit.jupiter.params.provider.Arguments
import org.mockito.Mockito
import org.springframework.kafka.support.Acknowledgment
import java.util.stream.Stream

object EventTestParameters {
    const val PATH = "ac.kr.smu.endticket.futureme.event.EventTestParameters"
    const val USER_ID = 1L
    val FUTURE_ME = FutureMe.from(CreateFutureMeRequest(CharacterType.VEGA), USER_ID)

    @JvmStatic
    fun provideTicketCompletedEventRecordAndAck() =
        Stream.of(
            Arguments.of(
                (Mockito.mock(ConsumerRecord::class.java) as ConsumerRecord<String, TicketCompletedEventResponse>)
                    .also {
                        Mockito.`when`(it.value()).thenReturn(
                            TicketCompletedEventResponse(1L),
                        )
                        Mockito.`when`(it.key())
                            .thenReturn(USER_ID.toString())
                    },
                Mockito.mock(Acknowledgment::class.java),
            ),
        )

    @JvmStatic
    fun provideImaginationCompletedEvent() =
        Stream.of(
            Arguments.of(
                ImaginationCompletedEvent(
                    Imagination.from(
                        ImaginationParameters.REQUEST,
                        USER_ID,
                    ),
                ),
            ),
        )
}

package ac.kr.smu.endticket.futureme.event

import ac.kr.smu.endticket.futureme.domain.event.model.ImaginationCompletedEvent
import ac.kr.smu.endticket.futureme.domain.imagination.model.Imagination
import ac.kr.smu.endticket.futureme.imagination.REQUEST
import ac.kr.smu.endticket.futureme.imagination.USER_ID
import ac.kr.smu.endticket.futureme.infra.messaging.TicketCompletedEventResponse
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.junit.jupiter.params.provider.Arguments
import org.mockito.Mockito
import org.springframework.kafka.support.Acknowledgment
import java.util.stream.Stream

object EventTestParameters {
    const val PATH = "ac.kr.smu.endticket.futureme.event.EventTestParameters"
    @JvmStatic
    fun provideTicketCompletedEventRecordAndAck() = Stream.of(
        Arguments.of(
            (Mockito.mock(ConsumerRecord::class.java) as ConsumerRecord<String, TicketCompletedEventResponse>)
                .also {
                    Mockito.`when`(it.value()).thenReturn(
                        TicketCompletedEventResponse(1L)
                    )
                },
            Mockito.mock(Acknowledgment::class.java)
        )
    )
    @JvmStatic
    fun provideImaginationCompletedEvent() = Stream.of(Arguments.of(ImaginationCompletedEvent(Imagination.from(REQUEST, USER_ID))))
}
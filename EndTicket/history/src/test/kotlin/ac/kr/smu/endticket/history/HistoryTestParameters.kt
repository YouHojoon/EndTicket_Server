package ac.kr.smu.endticket.history

import ac.kr.smu.endticket.common.kafka.constant.KafkaTopic
import ac.kr.smu.endticket.common.web.enum.CharacterType
import ac.kr.smu.endticket.common.web.enum.Color
import ac.kr.smu.endticket.common.web.enum.TicketType
import ac.kr.smu.endticket.history.domain.model.History
import ac.kr.smu.endticket.history.domain.model.ImaginationHistory
import ac.kr.smu.endticket.history.domain.model.TicketHistory
import ac.kr.smu.endticket.history.infra.messaging.EventResponse
import ac.kr.smu.endticket.history.infra.messaging.ImaginationCompletedEventResponse
import ac.kr.smu.endticket.history.infra.messaging.TicketCompletedEventResponse
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.junit.jupiter.params.provider.Arguments
import org.mockito.Mockito
import java.time.LocalDateTime
import java.util.stream.Stream
import kotlin.reflect.KClass

object HistoryTestParameters{
    const val USER_ID = 1L
    const val PATH = "ac.kr.smu.endticket.history.HistoryTestParameters"

    private val TICKET_COMPLETED_EVENT_RESPONSE = TicketCompletedEventResponse(
        id = 1L,
        behavior = "behavior",
        target = "target",
        color = Color.BLUE1,
        type = TicketType.HEALTH,
        swipeCount = 5,
        completedAt = LocalDateTime.now()
    )

    private val IMAGINATION_COMPLETED_EVENT_RESPONSE = ImaginationCompletedEventResponse(
        id = 1L,
        behavior = "behavior",
        target = "target",
        color = Color.BLUE1,
        characterType = CharacterType.VEGA,
        completedAt = LocalDateTime.now()
    )

    @JvmStatic
    fun provideHistorySpecificIdAndType() = Stream.of(
        Arguments.of(TicketHistory.from(TICKET_COMPLETED_EVENT_RESPONSE, USER_ID), TICKET_COMPLETED_EVENT_RESPONSE.id, History.Type.TICKET),
        Arguments.of(ImaginationHistory.from(IMAGINATION_COMPLETED_EVENT_RESPONSE, USER_ID), IMAGINATION_COMPLETED_EVENT_RESPONSE.id, History.Type.IMAGINATION)
    )

    @JvmStatic
    fun provideHistoriesAndType() = Stream.of(
        Arguments.of(setOf(TicketHistory.from(TICKET_COMPLETED_EVENT_RESPONSE, USER_ID)), History.Type.TICKET),
        Arguments.of(setOf(ImaginationHistory.from(IMAGINATION_COMPLETED_EVENT_RESPONSE, USER_ID)),History.Type.IMAGINATION)
    )

    @JvmStatic
    fun provideTopicAndResponseAndType() = Stream.of(
        Arguments.of(KafkaTopic.TICKET_COMPLETED, TICKET_COMPLETED_EVENT_RESPONSE, TicketHistory::class),
        Arguments.of(KafkaTopic.IMAGINATION_COMPLETED, IMAGINATION_COMPLETED_EVENT_RESPONSE, ImaginationHistory::class)
    )

    @JvmStatic
    fun provideRecord() = Stream.of(
        *EventResponse::class.sealedSubclasses.map { Arguments.of(mockRecord(it)) }.toTypedArray()
    )

    @JvmStatic
    fun provideRecordAndType() = Stream.of(
        *EventResponse::class.sealedSubclasses.map { Arguments.of(mockRecord(it), historyTypeOfResponse(it)) }.toTypedArray()
    )

    @JvmStatic
    fun provideHistoriesOfEachType() = Stream.of(
        Arguments.of(
            setOf(TicketHistory.from(TICKET_COMPLETED_EVENT_RESPONSE, USER_ID)),
            setOf(
                ImaginationHistory.from(IMAGINATION_COMPLETED_EVENT_RESPONSE, USER_ID),
                ImaginationHistory.from(
                    ImaginationCompletedEventResponse(
                        id = 2,
                        behavior = "behavior",
                        target = "target",
                        characterType = CharacterType.KIA,
                        completedAt = LocalDateTime.now(),
                        color = Color.BLUE1
                    )
                    ,USER_ID
                )
            )
        )
    )
    /**
     * ConsumerRecord를 mocking 하는 메소드
     * @param responseType mocking할 EventResponse 타입
     * @return mocking된 ConsumerRecord
     */
    private fun mockRecord(responseType: KClass<out EventResponse>): ConsumerRecord<String, EventResponse> {
        val record = Mockito.mock(ConsumerRecord::class.java) as ConsumerRecord<String, EventResponse>
        val (topic,value) = when(responseType){
            TicketCompletedEventResponse::class -> KafkaTopic.TICKET_COMPLETED to TICKET_COMPLETED_EVENT_RESPONSE
            ImaginationCompletedEventResponse::class ->  KafkaTopic.IMAGINATION_COMPLETED to IMAGINATION_COMPLETED_EVENT_RESPONSE
            else -> throw IllegalArgumentException("$responseType 은 지원하지 않는 타입입니다.")
        }

        Mockito.`when`(record.key()).thenReturn(USER_ID.toString())
        Mockito.`when`(record.topic()).thenReturn(topic)
        Mockito.`when`(record.value()).thenReturn(value)

        return record
    }

    /**
     * 이벤트 응답에 맞는 기록 class을 반환하는 메소드
     * @param responseType 이벤트 응답
     * @return 기록 class
     */
    private fun historyTypeOfResponse(responseType: KClass<out EventResponse>):History.Type = when(responseType){
        TicketCompletedEventResponse::class -> History.Type.TICKET
        ImaginationCompletedEventResponse::class ->  History.Type.IMAGINATION
        else -> throw IllegalArgumentException("$responseType 은 지원하지 않는 타입입니다.")
    }
}


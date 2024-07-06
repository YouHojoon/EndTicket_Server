package ac.kr.smu.endticket.history

import ac.kr.smu.endticket.common.kafka.constant.KafkaTopic
import ac.kr.smu.endticket.common.web.enum.CharacterType
import ac.kr.smu.endticket.common.web.enum.Color
import ac.kr.smu.endticket.common.web.enum.TicketType
import ac.kr.smu.endticket.history.domain.model.History
import ac.kr.smu.endticket.history.domain.model.ImaginationHistory
import ac.kr.smu.endticket.history.domain.model.TicketHistory
import ac.kr.smu.endticket.history.infra.messaging.ImaginationCompletedEventResponse
import ac.kr.smu.endticket.history.infra.messaging.TicketCompletedEventResponse
import org.junit.jupiter.params.provider.Arguments
import java.time.LocalDateTime
import java.util.stream.Stream

object HistoryTestParameters {
    const val USER_ID = 1L
    const val PATH = "ac.kr.smu.endticket.history.HistoryTestParameters"

    private val TICKET_COMPLETED_EVENT_RESPONSE =
        TicketCompletedEventResponse(
            id = 1L,
            behavior = "behavior",
            target = "target",
            color = Color.BLUE1,
            type = TicketType.HEALTH,
            swipeCount = 5,
            completedAt = LocalDateTime.now(),
        )

    private val IMAGINATION_COMPLETED_EVENT_RESPONSE =
        ImaginationCompletedEventResponse(
            id = 1L,
            behavior = "behavior",
            target = "target",
            color = Color.BLUE1,
            characterType = CharacterType.VEGA,
            completedAt = LocalDateTime.now(),
        )

    @JvmStatic
    fun provideHistoryAndSpecificIdAndType() =
        Stream.of(
            Arguments.of(
                TicketHistory.from(TICKET_COMPLETED_EVENT_RESPONSE, USER_ID),
                TICKET_COMPLETED_EVENT_RESPONSE.id,
                History.Type.TICKET,
            ),
            Arguments.of(
                ImaginationHistory.from(IMAGINATION_COMPLETED_EVENT_RESPONSE, USER_ID),
                IMAGINATION_COMPLETED_EVENT_RESPONSE.id,
                History.Type.IMAGINATION,
            ),
        )

    @JvmStatic
    fun provideHistoriesAndType() =
        Stream.of(
            Arguments.of(setOf(TicketHistory.from(TICKET_COMPLETED_EVENT_RESPONSE, USER_ID)), History.Type.TICKET),
            Arguments.of(
                setOf(ImaginationHistory.from(IMAGINATION_COMPLETED_EVENT_RESPONSE, USER_ID)),
                History.Type.IMAGINATION,
            ),
        )

    @JvmStatic
    fun provideEventResponseAndUserId() =
        Stream.of(
            Arguments.of(TICKET_COMPLETED_EVENT_RESPONSE, USER_ID),
            Arguments.of(IMAGINATION_COMPLETED_EVENT_RESPONSE, USER_ID),
        )

    @JvmStatic
    fun provideTopicAndResponseAndType() =
        Stream.of(
            Arguments.of(
                KafkaTopic.TICKET_COMPLETED,
                TICKET_COMPLETED_EVENT_RESPONSE,
                History.Type.TICKET,
            ),
            Arguments.of(
                KafkaTopic.IMAGINATION_COMPLETED,
                IMAGINATION_COMPLETED_EVENT_RESPONSE,
                History.Type.IMAGINATION,
            ),
        )

    @JvmStatic
    fun provideHistoriesOfEachType() =
        Stream.of(
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
                            color = Color.BLUE1,
                        ),
                        USER_ID,
                    ),
                ),
            ),
        )
}

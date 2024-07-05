package ac.kr.smu.endticket.futureme.imagination

import ac.kr.smu.endticket.common.web.enum.Color
import ac.kr.smu.endticket.futureme.domain.imagination.exception.ImaginationNotFoundException
import ac.kr.smu.endticket.futureme.domain.imagination.exception.ImaginationOwnershipException
import ac.kr.smu.endticket.futureme.domain.imagination.model.Imagination
import ac.kr.smu.endticket.futureme.ui.request.ImaginationRequest
import org.junit.jupiter.params.provider.Arguments
import java.util.stream.Stream

object ImaginationParameters {
    const val PATH = "ac.kr.smu.endticket.futureme.imagination.ImaginationParameters"
    const val USER_ID = 1L

    val REQUEST =
        ImaginationRequest(
            "b",
            "t",
            Color.BLUE1,
        )

    val INVALID_BEHAVIOR_REQUEST =
        ImaginationRequest(
            behavior = "new behavior",
            target = "target",
            color = Color.GRAY2,
        )
    val INVALID_TARGET_REQUEST =
        ImaginationRequest(
            behavior = "behavior",
            target = "new target with exceed",
            color = Color.GRAY2,
        )

    val UPDATE_REQUEST = ImaginationRequest("new behav", "new target", Color.GREEN1)

    @JvmStatic
    fun provideImaginationAndRequest() =
        Stream.of(
            Arguments.of(Imagination.from(REQUEST, USER_ID), UPDATE_REQUEST),
        )

    @JvmStatic
    fun provideImagination() =
        Stream.of(
            Arguments.of(Imagination.from(REQUEST, USER_ID)),
        )

    @JvmStatic
    fun provideImaginations() =
        Stream.of(
            Arguments.of(setOf(Imagination.from(REQUEST, USER_ID))),
        )

    @JvmStatic
    fun provideImaginationResponses() =
        Stream.of(
            Arguments.of(setOf(Imagination.from(REQUEST, USER_ID).toResponse())),
        )

    @JvmStatic
    fun provideInvalidImaginationAndReqeuest() =
        Stream.of(
            Arguments.of(
                Imagination.from(REQUEST, USER_ID),
                UPDATE_REQUEST,
                2L,
                ImaginationOwnershipException::class,
            ),
            Arguments.of(
                null,
                UPDATE_REQUEST,
                USER_ID,
                ImaginationNotFoundException::class,
            ),
        )

    @JvmStatic
    fun provideInvalidImagination() =
        Stream.of(
            Arguments.of(
                Imagination.from(REQUEST, USER_ID),
                2L,
                ImaginationOwnershipException::class,
            ),
            Arguments.of(
                null,
                USER_ID,
                ImaginationNotFoundException::class,
            ),
        )

    @JvmStatic
    fun provideInvalidImaginationRequest() =
        Stream.of(
            Arguments.of(INVALID_BEHAVIOR_REQUEST),
            Arguments.of(INVALID_TARGET_REQUEST),
        )

    @JvmStatic
    fun provideImaginationResponseAndRequest() =
        Stream.of(
            Arguments.of(
                Imagination.from(REQUEST, USER_ID).toResponse(),
                REQUEST,
            ),
        )

    @JvmStatic
    fun provideInvalidIdAndRequest() =
        Stream.of(
            Arguments.of(REQUEST, 1L, USER_ID, ImaginationNotFoundException(1L), 404),
            Arguments.of(REQUEST, 1L, 2L, ImaginationOwnershipException(1L, 2L), 403),
        )

    @JvmStatic
    fun provideInvalidIdAndException() =
        Stream.of(
            Arguments.of(1L, USER_ID, ImaginationNotFoundException(1L), 404),
            Arguments.of(1L, 2L, ImaginationOwnershipException(1L, 2L), 403),
        )

    @JvmStatic
    fun provideInvalidId() =
        Stream.of(
            Arguments.of(2L, USER_ID, 404),
            Arguments.of(1L, 2L, 403),
        )
}

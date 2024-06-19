package ac.kr.smu.endticket.history.domain.model

import ac.kr.smu.endticket.common.jpa.Audit
import ac.kr.smu.endticket.history.infra.messaging.TicketCompletedEventResponse
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.persistence.*

/**
 * 티켓 기록
 * @property id 티켓의 id
 * @property behavior 행동
 * @property target 목표
 * @property color 색
 * @property type 분류
 * @property swipeCount 스와이프 횟수
 */
@Entity
@Table(indexes = [
    Index(name = "idx_user_id", columnList = "user_id")
])
class TicketHistory private constructor(
    @Id
    private val id: Long,
    @Column(updatable = false, nullable = false)
    private val behavior: String,

    @Column(updatable = false, nullable = false)
    private val target: String,

    @Column(updatable = false, nullable = false)
    private val color: Color,

    @Column(updatable = false, nullable = false)
    private val type: Type,

    @Column(updatable = false, nullable = false)
    private val swipeCount: Int,

    @Column(updatable = false, nullable = false)
    private val userId: Long
) {

    companion object{
        fun from(response: TicketCompletedEventResponse, userId: Long) = TicketHistory(
            id = response.id,
            behavior = response.behavior,
            target = response.target,
            color = response.color,
            type = response.type,
            swipeCount = response.swipeCount,
            userId = userId
        )
    }

    @Embedded
    val audit = Audit()

    @Schema(description = "분류")
    enum class Type{
        HEALTH, PERSONALITY, VALUE, SELF_IMPROVEMENT, RELATIONSHIP
    }

    @Schema(description = "티켓의 색")
    enum class Color(val value: String) {
        RED1("#E591A6"), RED2("#E28089"), RED3("#C56859"),
        ORANGE1("#EFCB7E"), ORANGE2("#EABD97"), ORANGE3("#E99D7B"),
        GREEN1("#88C7B2"), GREEN2("#83ABA5"), GREEN3("#4CA199"),
        BLUE1("#8DD3E8"), BLUE2("#7FBAD5"), BLUE3("#6D98DE"),
        PURPLE1("#B0BAF0"), PURPLE2("#A49CDA"), PURPLE3("#9F7E99"),
        GRAY1("#C2C8CF"), GRAY2("#A3A8B3"), GRAY3("#616871")
    }
}
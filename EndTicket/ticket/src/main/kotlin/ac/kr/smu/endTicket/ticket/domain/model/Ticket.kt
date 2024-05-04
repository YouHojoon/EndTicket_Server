package ac.kr.smu.endTicket.ticket.domain.model

import ac.kr.smu.endTicket.ticket.ui.request.TicketRequest
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.Table

/**
 * 티켓을 추상화한 클래스
 * @param behavior 행동
 * @param target 목적
 * @param color 티켓의 색깔
 * @param type 분류
 * @param swipeCount 스와이프 횟수
 * @param userID 티켓 소유자의 사용자 id
 */
@Entity
@Schema(description = "티켓")
@Table(name = "ticket", indexes = [
    Index(name = "idx_user_id", columnList = "user_id")
])
class   Ticket(
    @Column(nullable = false)
    @Schema(description = "행동", example = "힘들어도 눈치 보지 말고 꼭 대화하기")
    var behavior: String,

    @Schema(description = "목적", example = "많은 사람들 앞에서 당당한 내 모습")
    @Column(nullable = false)
    var target: String,

    @Schema(description = "티켓의 색", implementation = Color::class)
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    var color: Color,

    @Schema(description = "분류", implementation = Type::class)
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    var type: Type,

    @Schema(description = "스와이프 횟수", implementation = SwipeCount::class)
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    var swipeCount: SwipeCount,

    @Schema(description = "티켓의 소유자", example = "1")
    @Column(name = "user_id", updatable = false, nullable = false)
    val userID: Long
) {
    constructor(createTicketRequest: TicketRequest, userID: Long):
            this(
                behavior = createTicketRequest.behavior,
                target = createTicketRequest.target,
                color = createTicketRequest.color,
                type = createTicketRequest.type,
                swipeCount = createTicketRequest.swipeCount,
                userID = userID
                )

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0L


    @Schema(description = "스와이프 횟수")
    enum class SwipeCount(val value: Int){
        FIVE(5), TEN(10), FIFTEEN(15)
    }

    @Schema(description = "분류")
    enum class Type{
        HEALTH, PERSONALITY, VALUE, SELF_IMPORVEMENT, RELATIONSHIP
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

    override fun equals(other: Any?): Boolean {
        if (other == null)
            return false

        val o = other as? Ticket ?: return false

        return o.id == id
    }

    fun update(request: TicketRequest){
        this.behavior = request.behavior
        this.target = request.target
        this.color = request.color
        this.swipeCount =request.swipeCount
        this.type = request.type

    }
}
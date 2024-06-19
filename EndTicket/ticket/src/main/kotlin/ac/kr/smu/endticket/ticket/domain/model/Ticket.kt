package ac.kr.smu.endticket.ticket.domain.model

import ac.kr.smu.endticket.common.jpa.Audit
import ac.kr.smu.endticket.ticket.domain.converter.MaxSwipeCountConverter
import ac.kr.smu.endticket.ticket.domain.exception.TicketOwnershipException
import ac.kr.smu.endticket.ticket.ui.request.TicketRequest
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.persistence.*
import org.springframework.data.jpa.domain.support.AuditingEntityListener

/**
 * 티켓을 추상화한 클래스
 * @property behavior 행동
 * @property target 목표
 * @property color 티켓의 색깔
 * @property type 분류
 * @property maxSwipeCount 최대 스와이프 횟수
 * @property userId 티켓 소유자의 사용자 id
 */
@Entity
@Table(name = "ticket", indexes = [
    Index(name = "idx_user_id", columnList = "user_id")
])
@EntityListeners(AuditingEntityListener::class)
class Ticket private constructor(
    behavior: String,
    target: String,
    color: Color,
    type: Type,
    maxSwipeCount: MaxSwipeCount,

    @Column(name = "user_id", updatable = false, nullable = false)
    val userId: Long,
){
    @Column(nullable = false, length = 20)
    var behavior: String private set
    @Column(nullable = false, length = 20)
    var target: String private set

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    var color: Color private set

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    var type: Type private set

    @Column(nullable = false)
    @Convert(converter = MaxSwipeCountConverter::class)
    var maxSwipeCount: MaxSwipeCount private set

    init {
        this.behavior = behavior
        this.target = target
        this.color = color
        this.type = type
        this.maxSwipeCount = maxSwipeCount
    }
    companion object{
        /**
         * [TicketRequest] 로부터 티켓을 생성하는 메소드
         * @param request 생성에 이용할 요청
         * @param userId 티켓의 소유자
         */
        fun from(request: TicketRequest, userId: Long) =
            Ticket(
                behavior = request.behavior,
                target = request.target,
                color = request.color,
                type = request.type,
                maxSwipeCount = request.maxSwipeCount,
                userId = userId
            )
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0L

    @Column(nullable = false)
    var swipeCount: Int = 0
        private set

    @Embedded
    val audit = Audit()

    enum class MaxSwipeCount(val value: Int){
        FIVE(5), TEN(10), FIFTEEN(15);
        companion object {
            fun fromValue(value: Int): MaxSwipeCount {
                return values().firstOrNull { it.value == value } ?: throw IllegalArgumentException("$value 의 MaxSwipeCount가 존재하지 않습니다.")
            }
        }
    }

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

    override fun equals(other: Any?): Boolean {
        if (other == null)
            return false

        val o = other as? Ticket ?: return false

        return o.id == id
    }


    /**
     * 티켓의 수정 메소드
     * @param request 수정에 사용할 요청
     * @param userId 수정 요청을 한 사용자
     * @throws TicketOwnershipException 티켓의 소유자가 아닌 사용자가 요청했을 시
     */
    @Throws(TicketOwnershipException::class)
    fun updateAndCheckCompletion(request: TicketRequest, userId: Long): Boolean{
        checkOwnership(userId)

        this.behavior = request.behavior
        this.target = request.target
        this.color = request.color
        this.maxSwipeCount = request.maxSwipeCount
        this.type = request.type

        return maxSwipeCount.value <= swipeCount
    }

    /**
     * 티켓을 스와이프하고 완료를 확인하는 메소드
     * @return 완료 여부
     * @throws TicketOwnershipException 티켓의 소유자가 아닌 사용자가 요청했을 시
     */
    @Throws(TicketOwnershipException::class)
    fun swipeAndCheckCompletion(userId: Long): Boolean{
        checkOwnership(userId)

        if (swipeCount < maxSwipeCount.value)
            swipeCount++

        return swipeCount == maxSwipeCount.value
    }

    @Throws(TicketOwnershipException::class)
    fun cancelSwipeTicket(userId: Long){
        checkOwnership(userId)

        if (swipeCount != 0)
            swipeCount--

    }

    /**
     * 티켓의 소유권을 확인하는 메소드
     * @throws TicketOwnershipException 소유자가 아닐 시
     */
    @Throws(TicketOwnershipException::class)
    fun checkOwnership(userId: Long){
        if (userId != this.userId)
            throw TicketOwnershipException(id,userId)
    }
}
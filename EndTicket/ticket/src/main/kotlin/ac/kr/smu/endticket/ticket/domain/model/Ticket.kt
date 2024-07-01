package ac.kr.smu.endticket.ticket.domain.model

import ac.kr.smu.endticket.common.jpa.Audit
import ac.kr.smu.endticket.common.web.enum.Color
import ac.kr.smu.endticket.common.web.enum.TicketType
import ac.kr.smu.endticket.ticket.domain.converter.MaxSwipeCountConverter
import ac.kr.smu.endticket.ticket.domain.exception.TicketOwnershipException
import ac.kr.smu.endticket.ticket.infra.messaging.TicketCompletedEventResponse
import ac.kr.smu.endticket.ticket.ui.request.TicketRequest
import ac.kr.smu.endticket.ticket.ui.response.TicketResponse
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.persistence.*
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.LocalDateTime

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
    @Column(nullable = false, length = 20)
    private var behavior: String,

    @Column(nullable = false, length = 20)
    private var target: String,

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private var color: Color,

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private var type: TicketType,

    @Column(nullable = false)
    @Convert(converter = MaxSwipeCountConverter::class)
    private var maxSwipeCount: MaxSwipeCount,

    @Column(name = "user_id", updatable = false, nullable = false)
    val userId: Long,
){
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

    /**
     * 티켓의 최대 스와이프 횟수
     * @property FIVE 5회
     * @property TEN 10회
     * @property FIFTEEN 15회
     * @property value 각 횟수의 맞는 값
     */
    @Schema(description = "티켓의 최대 스와이프 횟수")
    enum class MaxSwipeCount(val value: Int){
        @Schema(description = "5회")
        FIVE(5),
        @Schema(description = "10회")
        TEN(10),
        @Schema(description = "15회")
        FIFTEEN(15);
        companion object {
            /**
             * value 로부터 티켓 최대 스와이프 횟수를 생성하는 메소드
             * @param value 생성을 원하는 값
             * @return 생성된 티켓 최대 스와이프 횟수
             * @throws IllegalArgumentException 지원하지 않는 value일 때
             */
            fun fromValue(value: Int): MaxSwipeCount {
                return values().firstOrNull { it.value == value } ?: throw IllegalArgumentException("$value 의 MaxSwipeCount가 존재하지 않습니다.")
            }
        }
    }

    override fun equals(other: Any?): Boolean {
        if (other == null)
            return false

        val o = other as? Ticket ?: return false

        return o.id == id
    }

    /**
     * 티켓으로부터 응답을 생성하는 메소드
     * @return 생성된 응답
     */
    fun toResponse() = TicketResponse(
        id = id,
        behavior = behavior,
        target = target,
        type = type,
        color = color,
        swipeCount = swipeCount,
        maxSwipeCount = maxSwipeCount
    )

    /**
     * 티켓으로부터 이벤트 완료 응답을 생성하는 메소드
     * @return 이벤트 완료 응답
     */
    fun toEventResponse() = TicketCompletedEventResponse(
        id = id,
        behavior = behavior,
        target = target,
        type = type,
        color = color,
        swipeCount = swipeCount,
        completedAt = audit.updatedAt ?: LocalDateTime.now()
    )

    /**
     * 티켓의 수정 메소드
     * @param request 수정에 사용할 요청
     * @param userId 수정 요청을 한 사용자
     * @throws TicketOwnershipException 티켓의 소유자가 아닌 사용자가 요청했을 시
     */
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
     * @param userId 소유자 Id
     * @return 완료 여부
     * @throws TicketOwnershipException 티켓의 소유자가 아닌 사용자가 요청했을 시
     */
    fun swipeAndCheckCompletion(userId: Long): Boolean{
        checkOwnership(userId)

        if (swipeCount < maxSwipeCount.value)
            swipeCount++

        return swipeCount == maxSwipeCount.value
    }

    /**
     * 티켓의 스와이프를 취소하는 메소드, 0회 이하로는 내려가지 않는다.
     * @param userId 소유자 Id
     * @throws TicketOwnershipException 소유자가 아닐 시
     */
    fun cancelSwipeTicket(userId: Long){
        checkOwnership(userId)

        if (swipeCount != 0)
            swipeCount--

    }

    /**
     * 티켓의 소유권을 확인하는 메소드
     * @throws TicketOwnershipException 소유자가 아닐 시
     */
    fun checkOwnership(userId: Long){
        if (userId != this.userId)
            throw TicketOwnershipException(id,userId)
    }
}
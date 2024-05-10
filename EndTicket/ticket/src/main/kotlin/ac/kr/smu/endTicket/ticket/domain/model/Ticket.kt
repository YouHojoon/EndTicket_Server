package ac.kr.smu.endTicket.ticket.domain.model

import ac.kr.smu.endTicket.ticket.domain.exception.NotOwnerOfTicketException
import ac.kr.smu.endTicket.ticket.ui.request.TicketRequest
import ac.kr.smu.endTicket.ticket.ui.response.TicketResponse
import com.fasterxml.jackson.annotation.JsonIgnore
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
 * @property behavior 행동
 * @property target 목표
 * @property color 티켓의 색깔
 * @property type 분류
 * @property maxSwipeCount 최대 스와이프 횟수
 * @property userID 티켓 소유자의 사용자 id
 */
@Entity
@Table(name = "ticket", indexes = [
    Index(name = "idx_user_id", columnList = "user_id")
])
class Ticket(
    @Column(nullable = false)
    var behavior: String,

    @Column(nullable = false)
    var target: String,

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    var color: Color,

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    var type: Type,

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    var maxSwipeCount: MaxSwipeCount,

    @Column(name = "user_id", updatable = false, nullable = false)
    val userID: Long
) {
    /**
     * @constructor TicketRequest와 userID를 이용해서 생성하는 생성자
     * @param request 생성에 이용할 요청
     * @param userID 티켓의 소유자
     */
    constructor(request: TicketRequest, userID: Long):
            this(
                behavior = request.behavior,
                target = request.target,
                color = request.color,
                type = request.type,
                maxSwipeCount = request.maxSwipeCount,
                userID = userID
                )

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0L

    @Column(nullable = false)
    var swipeCount: Int = 0
        private set

    @Transient
    var shouldUpdate = false

    enum class MaxSwipeCount(val value: Int){
        FIVE(5), TEN(10), FIFTEEN(15)
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
     * @param userID 수정 요청을 한 사용자
     * @throws NotOwnerOfTicketException 티켓의 소유자가 아닌 사용자가 요청했을 시
     */
    @Throws(NotOwnerOfTicketException::class)
    fun update(request: TicketRequest, userID: Long){
        checkOwnership(userID)

        this.behavior = request.behavior
        this.target = request.target
        this.color = request.color
        this.maxSwipeCount = request.maxSwipeCount
        this.swipeCount = 0
        this.type = request.type

    }

    /**
     * 티켓을 스와이프하고 완료를 확인하는 메소드
     * @return 완료 여부
     * @throws NotOwnerOfTicketException 티켓의 소유자가 아닌 사용자가 요청했을 시
     */
    @Throws(NotOwnerOfTicketException::class)
    fun swipeAndCheckCompletion(userID: Long): Boolean{
        checkOwnership(userID)

        if (swipeCount < maxSwipeCount.value) {
            setShouldUpdateTrue()
            swipeCount++
        }

        return swipeCount == maxSwipeCount.value
    }

    @Throws(NotOwnerOfTicketException::class)
    fun cancelSwipeTicket(userID: Long){
        checkOwnership(userID)

        if (swipeCount != 0){
            setShouldUpdateTrue()
            swipeCount--
        }
    }

    /**
     * 현재 티켓을 응답에 사용하는 객체로 변환하는 메소드
     */
    fun toTicketResponse(): TicketResponse = TicketResponse(
        id = id,
        behavior = behavior,
        target = target,
        type = type,
        color = color,
        swipeCount = swipeCount,
        maxSwipeCount = maxSwipeCount
    )

    /**
     * shouldUpdate가 false면 true로 변경하는 메소드
     */
    private fun setShouldUpdateTrue(){
        if (!shouldUpdate)
            shouldUpdate = true
    }

    /**
     * 티켓의 소유권을 확인하는 메소드
     * @throws NotOwnerOfTicketException 소유자가 아닐 시
     */
    @Throws(NotOwnerOfTicketException::class)
    private fun checkOwnership(userID: Long){
        if (userID != this.userID)
            throw NotOwnerOfTicketException(id,userID)
    }
}
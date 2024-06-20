package ac.kr.smu.endticket.history.domain.model

import ac.kr.smu.endticket.common.jpa.Audit
import ac.kr.smu.endticket.history.infra.messaging.TicketCompletedEventResponse
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.persistence.*

/**
 * 티켓 기록
 * @property ticketId 티켓의 id
 * @property behavior 행동
 * @property target 목표
 * @property color 색
 * @property type 분류
 * @property swipeCount 스와이프 횟수
 * @property userId 소유자 ID
 */
@Entity
@Table(indexes = [
    Index(name = "idx_ticket_id", columnList = "ticket_id", unique = true),
    Index(name = "idx_user_id", columnList = "user_id")
])
@PrimaryKeyJoinColumn
class TicketHistory private constructor(
    @Column(updatable = false, nullable = false)
    private val ticketId: Long,
    @Column(updatable = false, nullable = false, length = 20)
    private val behavior: String,

    @Column(updatable = false, nullable = false, length = 20)
    private val target: String,

    @Column(updatable = false, nullable = false)
    private val color: Color,

    @Column(updatable = false, nullable = false)
    private val type: Type,

    @Column(updatable = false, nullable = false)
    private val swipeCount: Int,

    @Column(updatable = false, nullable = false)
    private val userId: Long
): History() {

    companion object{
        /**
         * 티켓 완료 이벤트로부터 티켓 기록 생성
         * @param response 티켓 완료 응답
         * @param userId 소유자 ID
         * @return 티켓 기록
         */
        fun from(response: TicketCompletedEventResponse, userId: Long) = TicketHistory(
            ticketId = response.id,
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
}
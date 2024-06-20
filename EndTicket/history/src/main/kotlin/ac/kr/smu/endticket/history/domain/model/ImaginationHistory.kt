package ac.kr.smu.endticket.history.domain.model

import ac.kr.smu.endticket.common.web.enum.Color
import ac.kr.smu.endticket.history.infra.messaging.ImaginationCompletedEventResponse
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Index
import jakarta.persistence.PrimaryKeyJoinColumn
import jakarta.persistence.Table

/**
 * 상상해보기 기록
 * @property imaginationId 상상해보기 ID
 * @property behavior 행동
 * @property target 목표
 * @property color 색
 * @property userId 소유자 ID
 */
@Entity
@Table(
    indexes = [
        Index(name = "idx_imagination_id", columnList = "imagination_id", unique = true),
        Index(name = "idx_user_id", columnList = "user_id")
    ]
)
@PrimaryKeyJoinColumn
class ImaginationHistory private constructor(
    @Column(updatable = false, nullable = false)
    private val imaginationId: Long,
    @Column(updatable = false, nullable = false, length = 10)
    private val behavior: String,
    @Column(updatable = false, nullable = false, length = 20)
    private val target: String,
    @Column(updatable = false, nullable = false)
    private val color: Color,
    @Column(updatable = false, nullable = false)
    private val userId: Long
): History(){
    companion object{
        /**
         * 상상해보기 완료 이벤트로부터 상상해보기 기록을 생성
         * @param response 상상해보기 완료 이벤트
         * @param userId 소유자 ID
         * @return 상상해보기 기록
         */
        fun from(response: ImaginationCompletedEventResponse, userId: Long) = ImaginationHistory(
            imaginationId = response.id,
            behavior = response.behavior,
            target = response.target,
            color = response.color,
            userId = userId
        )
    }
}
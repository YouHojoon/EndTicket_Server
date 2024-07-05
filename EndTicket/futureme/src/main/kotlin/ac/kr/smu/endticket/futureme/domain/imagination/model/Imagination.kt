package ac.kr.smu.endticket.futureme.domain.imagination.model

import ac.kr.smu.endticket.common.jpa.Audit
import ac.kr.smu.endticket.common.web.enum.CharacterType
import ac.kr.smu.endticket.common.web.enum.Color
import ac.kr.smu.endticket.futureme.domain.imagination.exception.ImaginationOwnershipException
import ac.kr.smu.endticket.futureme.infra.messaging.ImaginationCompletedEventResponse
import ac.kr.smu.endticket.futureme.ui.request.ImaginationRequest
import ac.kr.smu.endticket.futureme.ui.response.ImaginationResponse
import jakarta.persistence.*
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.LocalDateTime

/**
 * 상상해보기를 추상화한 객체
 * @property behavior 행동
 * @property target 목표
 * @property color 색
 * @property futureMe 연관되어 있는 미래의 나
 */
@Entity
@Table(
    indexes = [
        Index(name = "idx_user_id", columnList = "user_id"),
    ],
)
@EntityListeners(AuditingEntityListener::class)
class Imagination private constructor(
    @Column(nullable = false, length = 10)
    private var behavior: String,
    @Column(nullable = false, length = 20)
    private var target: String,
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private var color: Color,
    @Column(name = "user_id", updatable = false, nullable = false)
    val userId: Long,
) {
    companion object {
        fun from(
            request: ImaginationRequest,
            userId: Long,
        ) = Imagination(
            behavior = request.behavior,
            target = request.target,
            color = request.color,
            userId = userId,
        )
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id = 0L

    @Embedded
    val audit: Audit = Audit()

    @Column
    private var isComplete = false

    /**
     * 상상해보기로부터 응답을 만들어내는 메소드
     * @return 상상해보기 완료 응답
     */
    fun toResponse() =
        ImaginationResponse(
            id = id,
            behavior = behavior,
            target = target,
            color = color,
        )

    /**
     * 상상해보기로부터 이벤트 완료 응답을 만들어내는 메소드
     * @return 상상해보기 완료 응답
     */
    fun toEventResponse(characterType: CharacterType) =
        ImaginationCompletedEventResponse(
            id = id,
            behavior = behavior,
            target = target,
            color = color,
            characterType = characterType,
            completedAt = audit.updatedAt ?: LocalDateTime.now(),
        )

    /**
     * 수정을 요청하는 메소드
     * @param request 수정 요청
     * @param userId 수정을 요청한 사용자
     * @throws ImaginationOwnershipException 사용자가 소유자가 아닐 시
     */
    fun update(
        request: ImaginationRequest,
        userId: Long,
    ) {
        checkOwnership(userId)

        this.color = request.color
        this.behavior = request.behavior
        this.target = request.target
    }

    /**
     * 상상해보기 완료를 요청하는 메소드
     * @param userId 완료를 요청한 사용자
     * @throws ImaginationOwnershipException 사용자가 소유자가 아닐 시
     */
    fun complete(userId: Long) {
        checkOwnership(userId)
        isComplete = true
    }

    /**
     * 소유권을 확인하는 메소드
     * @param userId 사용자 Id
     * @throws ImaginationOwnershipException 사용자가 소유자가 아닐 시
     */
    fun checkOwnership(userId: Long) {
        if (this.userId != userId) {
            throw ImaginationOwnershipException(id, userId)
        }
    }
}

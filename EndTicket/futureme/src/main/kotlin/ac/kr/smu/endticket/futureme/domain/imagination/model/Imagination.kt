package ac.kr.smu.endticket.futureme.domain.imagination.model

import ac.kr.smu.endticket.common.jpa.Audit
import ac.kr.smu.endticket.futureme.domain.imagination.exception.ImaginationOwnershipException
import ac.kr.smu.endticket.futureme.ui.request.ImaginationRequest
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.persistence.*
import org.springframework.data.jpa.domain.support.AuditingEntityListener

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
        Index(name = "idx_user_id", columnList = "user_id")
    ]
)
@EntityListeners(AuditingEntityListener::class)
class Imagination private constructor(
    behavior: String,
    target: String,
    color: Color,
    @Column(name = "user_id", updatable = false, nullable = false)
    val userId: Long
) {
    companion object{
        fun from(request: ImaginationRequest, userId: Long) =
            Imagination(
                behavior = request.behavior,
                target = request.target,
                color = request.color,
                userId = userId
            )
    }
    @Column(nullable = false, length = 10)
    var behavior: String  = ""
        protected set

    @Column(nullable = false, length = 20)
    var target: String =""
    protected set

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var color: Color = Color.GRAY2
    protected set

    init {
        this.behavior = behavior
        this.target = target
        this.color = color
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

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id = 0L

    @Embedded
    val audit: Audit = Audit()

    @Column
    private var isComplete = false

    /**
     * 수정을 요청하는 메소드
     * @param request 수정 요청
     * @param userId 수정을 요청한 사용자
     */
    fun update(request: ImaginationRequest, userId: Long){
        checkOwnership(userId)

        this.color = request.color
        this.behavior = request.behavior
        this.target = request.target
    }


    /**
     * 상상해보기 완료를 요청하는 메소드
     * @param userId 완료를 요청한 사용자
     */
    fun complete(userId: Long){
        checkOwnership(userId)
        isComplete = true
    }

    /**
     * 소유권을 확인하는 메소드
     * @param userId 사용자 Id
     * @throws ImaginationOwnershipException 사용자가 소유자가 아닐 시
     */
    @Throws(ImaginationOwnershipException::class)
    fun checkOwnership(userId: Long){
        if (this.userId != userId)
            throw ImaginationOwnershipException(id, userId)

    }
}
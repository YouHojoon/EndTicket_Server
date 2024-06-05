package ac.kr.smu.endTicket.futureMe.domain.imagination.model

import ac.kr.smu.endTicket.common.jpa.Audit
import ac.kr.smu.endTicket.futureMe.domain.futureMe.model.FutureMe
import ac.kr.smu.endTicket.futureMe.ui.request.ImaginationRequest
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.persistence.*
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import kotlin.jvm.Transient

/**
 * 상상해보기를 추상화한 객체
 * @property behavior 행동
 * @property target 목표
 * @property color 색
 * @property futureMe 연관되어 있는 미래의 나
 */
@Entity
@Table
@EntityListeners(AuditingEntityListener::class)
class Imagination private constructor(
    behavior: String,
    target: String,
    color: Color,
    @Column(updatable = false, nullable = false)
    val userID: Long
) {
    companion object{
        fun from(request: ImaginationRequest, userID: Long) =
            Imagination(
                behavior = request.behavior,
                target = request.target,
                color = request.color,
                userID = userID
            )
    }
    @Column(nullable = false, length = 10)
    var behavior: String private set

    @Column(nullable = false, length = 20)
    var target: String private set

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var color: Color private set

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
     * @param userID 수정을 요청한 사용자
     */
    fun update(request: ImaginationRequest, userID: Long){
        checkOwnership(userID)

        this.color = request.color
        this.behavior = request.behavior
        this.target = request.target
    }


    /**
     * 상상해보기 완료를 요청하는 메소드
     * @param userID 완료를 요청한 사용자
     */
    fun complete(userID: Long){
        checkOwnership(userID)
        isComplete = true
    }

    /**
     * 소유권을 확인하는 메소드
     * @param userID 사용자 ID
     * @throws IllegalStateException 사용자가 소유자가 아닐 시
     */
    @Throws(IllegalStateException::class)
    fun checkOwnership(userID: Long){
        if (this.userID != userID)
            throw IllegalStateException("소유자가 아닙니다.")

    }
}
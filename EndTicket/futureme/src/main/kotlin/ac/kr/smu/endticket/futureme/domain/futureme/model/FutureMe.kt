package ac.kr.smu.endticket.futureme.domain.futureme.model

import ac.kr.smu.endticket.common.jpa.Audit
import ac.kr.smu.endticket.common.web.enum.CharacterType
import ac.kr.smu.endticket.futureme.domain.event.model.Event
import ac.kr.smu.endticket.futureme.ui.request.CreateFutureMeRequest
import ac.kr.smu.endticket.futureme.ui.request.UpdateFutureMeRequest
import ac.kr.smu.endticket.futureme.ui.response.FutureMeResponse
import jakarta.persistence.*
import org.springframework.data.jpa.domain.support.AuditingEntityListener

/**
 * 미래의 나를 추상화한 객체
 * @property userId 사용자 Id
 */
@Entity
@Table(
    indexes = [
        Index(name = "idx_user_id", columnList = "user_id"),
    ],
)
@EntityListeners(AuditingEntityListener::class)
class FutureMe private constructor(
    type: CharacterType,
    @Id
    @Column(name = "user_id", updatable = false, nullable = false)
    val userId: Long,
) {
    @Column(length = 13)
    private var title: String = ""

    @Embedded
    private var character: Character

    @Embedded
    private val audit: Audit = Audit()

    companion object {
        /**
         * 생성 요청으로 부터 미래의 나를 생성하는 메소드
         * @param request 요청
         * @param userId 요청한 사용자 Id
         * @return 생성된 미래의 나
         */
        fun from(
            request: CreateFutureMeRequest,
            userId: Long,
        ) = FutureMe(request.type, userId)
    }

    init {
        character = Character(type)
    }

    val characterType = character.type

    /**
     * 제목을 수정하는 메소드
     * @param request 수정 요청
     */
    fun update(request: UpdateFutureMeRequest) {
        val title = request.title
        val type = request.characterType

        if (request.isEmpty()) {
            throw IllegalArgumentException("요청이 비어있습니다.")
        }

        if (type != null) {
            this.character = Character(type)
        }

        if (title != null) {
            this.title = title
        }
    }

    /**
     * 이벤트에 맞는 캐릭터 경험치 상승
     * @param event 발생한 이벤트
     */
    fun gainExperiencePoints(event: Event) = character.gainExperiencePoints(event)

    /**
     * 미래의 나로부터 응답을 생성하는 메소드
     * @return 생성된 응답
     */
    fun toResponse() =
        FutureMeResponse(
            title = title,
            character = character.copy(),
        )
}

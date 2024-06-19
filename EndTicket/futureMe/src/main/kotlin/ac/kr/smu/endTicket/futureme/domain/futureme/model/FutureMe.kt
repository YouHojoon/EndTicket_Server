package ac.kr.smu.endTicket.futureme.domain.futureme.model

import ac.kr.smu.endTicket.common.jpa.Audit
import ac.kr.smu.endTicket.futureme.ui.request.CreateFutureMeRequest
import ac.kr.smu.endTicket.futureme.ui.request.UpdateFutureMeRequest
import jakarta.persistence.*
import org.springframework.data.jpa.domain.support.AuditingEntityListener

/**
 * 미래의 나를 추상화한 객체
 * @property userId 사용자 Id
 */
@Entity
@Table
@EntityListeners(AuditingEntityListener::class)
class FutureMe private constructor(
    type: Character.Type,

    @Id
    val userId: Long
) {
    @Column(length = 13)
    var title: String = ""
        private set

    @Embedded
    var character: Character
        private set

    @Embedded
    val audit: Audit = Audit()

    companion object{
        /**
         * 생성 요청으로 부터 미래의 나를 생성하는 메소드
         * @param request 요청
         * @param userId 요청한 사용자 Id
         * @return 생성된 미래의 나
         */
        fun from(request: CreateFutureMeRequest, userId: Long) = FutureMe(request.type, userId)
    }

    init {
        character = Character(type)
    }
    /**
     * 제목을 수정하는 메소드
     * @param request 수정 요청
     */
    fun update(request: UpdateFutureMeRequest){
        val title = request.title
        val type = request.type

        if (request.isEmpty())
            throw IllegalArgumentException("요청이 비어있습니다.")

        if (type != null)
            this.character = Character(type)

        if (title != null)
            this.title = title
    }
}
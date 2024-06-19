package ac.kr.smu.endTicket.futureMe.domain.futureMe.model

import ac.kr.smu.endticket.common.jpa.Audit
import ac.kr.smu.endTicket.futureMe.ui.request.CreateFutureMeRequest
import ac.kr.smu.endTicket.futureMe.ui.request.UpdateFutureMeRequest
import jakarta.persistence.*
import org.springframework.data.jpa.domain.support.AuditingEntityListener

/**
 * 미래의 나를 추상화한 객체
 * @property userID 사용자 ID
 */
@Entity
@Table
@EntityListeners(AuditingEntityListener::class)
class FutureMe private constructor(
    type: Character.Type,

    @Id
    val userID: Long
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
         * @param userID 요청한 사용자 ID
         * @return 생성된 미래의 나
         */
        fun from(request: CreateFutureMeRequest, userID: Long) = FutureMe(request.type, userID)
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
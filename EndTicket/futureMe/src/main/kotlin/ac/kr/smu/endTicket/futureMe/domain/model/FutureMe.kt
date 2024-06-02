package ac.kr.smu.endTicket.futureMe.domain.model

import ac.kr.smu.endTicket.futureMe.ui.request.UpdateTitleOfFutureMeRequest
import jakarta.persistence.*

/**
 * 미래의 나를 추상화한 객체
 * @property userID 사용자 ID
 */
@Entity
@Table
class FutureMe(
    @Id
    val userID: Long
) {
    @Column(length = 13)
    var title: String = ""
        private set

    @Embedded
    var character: Character? = null
        private set
    /**
     * 제목을 업데이트 하는 메소드
     */
    fun updateTitle(request: UpdateTitleOfFutureMeRequest){
        this.title = request.title
    }

    fun setCharacter(type: Character.Type, userID: Long){
        if (userID != this.userID)
            throw IllegalStateException("소유자가 아닙니다.")

        character = Character(type)
    }
}
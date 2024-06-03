package ac.kr.smu.endTicket.futureMe.domain.futureMe.model

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
    val userID: Long,
    type: Character.Type
) {
    @Column(length = 13)
    var title: String = ""
        private set

    @Embedded
    var character: Character
        private set

    init {
        character = Character(type)
    }

    /**
     * 제목을 수정하는 메소드
     * @param request 제못 수정 요청
     */
    fun updateTitle(request: UpdateTitleOfFutureMeRequest){
        this.title = request.title
    }

    /**
     * 캐릭터를 설정하는 메소드, 이미 캐릭터가 있다면 초기화된다.
     * @param type 캐릭터의 종류
     * @param userID 설정을 요청하는 사용자 ID
     * @throws IllegalStateException 소유자가 아닐 때
     */
    @Throws(IllegalStateException::class)
    fun setCharacter(type: Character.Type, userID: Long){
        if (userID != this.userID)
            throw IllegalStateException("소유자가 아닙니다.")

        character = Character(type)
    }
}
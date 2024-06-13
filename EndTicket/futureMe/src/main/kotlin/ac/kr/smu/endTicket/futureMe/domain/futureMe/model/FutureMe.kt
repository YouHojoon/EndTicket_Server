package ac.kr.smu.endTicket.futureMe.domain.futureMe.model

import ac.kr.smu.endTicket.common.jpa.Audit
import ac.kr.smu.endTicket.futureMe.ui.request.FutureMeCharacterRequest
import ac.kr.smu.endTicket.futureMe.ui.request.UpdateFutureMeTitleRequest
import com.fasterxml.jackson.annotation.JsonIgnore
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.persistence.*
import org.springframework.data.jpa.domain.support.AuditingEntityListener

/**
 * 미래의 나를 추상화한 객체
 * @property userID 사용자 ID
 */
@Entity
@Table
@Schema(description = "미래의 나")
@EntityListeners(AuditingEntityListener::class)
class FutureMe private constructor(
    type: Character.Type,

    @Id
    val userID: Long
) {
    @Column(length = 13)
    @Schema(description = "미래의 나 제목", example = "당당하고 멋있는 사람")
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
        fun from(request: FutureMeCharacterRequest, userID: Long) = FutureMe(request.type, userID)
    }

    init {
        character = Character(type)
    }
    /**
     * 제목을 수정하는 메소드
     * @param request 제못 수정 요청
     */
    fun updateTitle(request: UpdateFutureMeTitleRequest){
        this.title = request.title
    }

    /**
     * 캐릭터를 수정하는 메소드, 캐릭터의 정보들은 초기화된다.
     * @param type 캐릭터의 종류
     * @param userID 설정을 요청하는 사용자 ID
     * @throws IllegalStateException 소유자가 아닐 때
     */
    @Throws(IllegalStateException::class)
    fun updateCharacter(request: FutureMeCharacterRequest, userID: Long){
        if (userID != this.userID)
            throw IllegalStateException("소유자가 아닙니다.")

        character = Character(request.type)
    }
}
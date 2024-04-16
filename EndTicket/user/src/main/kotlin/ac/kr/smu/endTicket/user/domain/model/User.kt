package ac.kr.smu.endTicket.user.domain.model

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint

/**
 * 사용자를 추상화한 클래스
 * @property nickname 사용자의 별명, 3~8 자 사이여야 한다.
 * @property socialType SNS 로그인의 타입
 * @property socialUserId 각 SNS 별 회원번호
 * @property id 회원번호
 */
@Entity
@Table(uniqueConstraints = [
    UniqueConstraint(columnNames = ["social_type", "social_user_number"])
])
class User(
    @Column(name="social_type", nullable = false, updatable = false)
    @Enumerated(value = EnumType.STRING)
    val socialType: SocialType,

    @Column(name = "social_user_number", nullable = false, updatable = false)
    private val socialUserNumber: String,

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false, insertable = false, updatable = false)
    val id: Long = 0L,

    nickname: String? = null
)
{
    @Column
    var nickname: String?
        private set
    init {
        this.nickname = nickname
    }

    enum class SocialType {
        KAKAO, GOOGLE, APPLE
    }

    override fun equals(other: Any?): Boolean {
        val user = (other as? User) ?: return false

        return user.id == other.id
    }

    /**
     * 닉네임을 변경하는 메소드
     * @param nickname 변경할 닉네임
     * @throws IllegalStateException 닉네임이 null이 아닐 떄
     */
    @Throws(IllegalStateException::class)
    fun updateNickname(nickname: String){
        check(this.nickname == null){"닉네임을 변경할 수 없습니다."}
        this.nickname = nickname
    }

    override fun toString(): String {
        return  "id: $id, nickname: $nickname, $socialType: $socialType"
    }
}
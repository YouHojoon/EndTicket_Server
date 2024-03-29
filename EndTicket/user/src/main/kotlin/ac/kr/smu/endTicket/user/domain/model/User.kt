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
    private val socialType: SocialType,

    @Column(name = "social_user_number", nullable = false, updatable = false)
    private val socialUserNumber: String,

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false, insertable = false, updatable = false)
    val id: Long = 0L,

    @Column(updatable = false)
    val nickname: String? = null
)
{
    enum class SocialType {
        KAKAO, GOOGLE, APPLE
    }

    override fun equals(other: Any?): Boolean {
        val user = (other as? User) ?: return false

        return user.id == other.id
    }
}
package ac.kr.smu.endTicket.user.domain.model

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id

/**
 * 사용자를 추상화한 클래스
 * @property nickname 사용자의 별명, 3~8 자 사이여야 한다.
 * @property socialType SNS 로그인의 타입
 * @property socialUserId 각 SNS 별 회원번호
 * @param id DB에 저장되는 primary key
 */
@Entity

class User(
    @Column(nullable = false)
    val nickname: String,

    @Column(nullable = false, updatable = false)
    @Enumerated(value = EnumType.STRING)
    private val socialType: SocialType,

    @Column(nullable = false, updatable = false)
    private val socialUserNumber: Long
)
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false, insertable = false, updatable = false)
    val id: Long = 0L
    enum class SocialType {
        KAKAO, GOOGLE, APPLE
    }

    override fun equals(other: Any?): Boolean {
        val user = (other as? User) ?: return false

        return user.socialUserNumber == socialUserNumber && socialType == user.socialType
    }
}
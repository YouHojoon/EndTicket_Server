package ac.kr.smu.endTicket.user.domain.model

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.persistence.*
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import java.lang.IllegalArgumentException

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
    private val socialUserNumber: String
)
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false, insertable = false, updatable = false)
    private val id: Long = 0L
    enum class SocialType {
        KAKAO, GOOGLE, APPLE
    }

    override fun equals(other: Any?): Boolean {
        val user = (other as? User) ?: return false

        return user.socialUserNumber == socialUserNumber && socialType == user.socialType
    }
}
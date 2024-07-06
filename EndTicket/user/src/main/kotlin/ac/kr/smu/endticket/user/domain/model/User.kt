package ac.kr.smu.endticket.user.domain.model

import ac.kr.smu.endticket.user.ui.request.NicknameRegisterRequest
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
 * 사용자
 * @property socialType SNS 로그인의 타입
 * @property socialUserNumber 각 SNS 별 회원번호
 */
@Entity
@Table(
    name = "\"user\"",
    uniqueConstraints = [UniqueConstraint(columnNames = ["social_type", "social_user_number"])],
)
class User(
    @Column(name = "social_type", nullable = false, updatable = false)
    @Enumerated(value = EnumType.STRING)
    val socialType: SocialType,
    @Column(name = "social_user_number", nullable = false, updatable = false)
    private val socialUserNumber: String,
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0L

    @Column(nullable = true)
    var nickname: String? = null
        private set

    /**
     * 사용자의 SNS 타입
     * @property KAKAO 카카오
     * @property GOOGLE 구글
     * @property APPLE 애플
     */
    enum class SocialType {
        KAKAO,
        GOOGLE,
        APPLE,
    }

    /**
     * 닉네임을 등록하는 메소드
     * @param request 등록 요청
     * @throws IllegalStateException 닉네임이 null이 아닐 떄
     */
    fun registerNickname(request: NicknameRegisterRequest) {
        check(this.nickname == null) { "닉네임을 변경할 수 없습니다." }
        this.nickname = request.nickname
    }

    override fun toString(): String = "{id: $id, nickname: $nickname, socialType: $socialType, socialUserNumber: $socialUserNumber}"
}

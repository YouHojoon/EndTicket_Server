package ac.kr.smu.endTicket.user.domain.model

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.persistence.*
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size

/**
 * 사용자를 추상화한 클래스
 * @property nickname 사용자의 별명, 3~8 자 사이여야 한다.
 * @property email 사용자의 이메일, 중복되어서는 안된다.
 * @property socialType SNS 로그인의 타입
 * @property socialUserId 각 SNS 별 회원번호
 * @param id DB에 저장되는 primary key
 */
@Entity
@Schema(description = "사용자")
class User(

    @field:Size(min = 3, max = 8)
    @Column(nullable = false)
    @Schema(description = "별명", nullable = false, example = "테스트")
    val nickname: String,

    @field:Email
    @Column(nullable = false, updatable = false, unique = true)
    @Schema(description = "이메일", nullable = false, example = "test@test.com")
    val email: String,

    @Column(nullable = false, updatable = false)
    @Enumerated(value = EnumType.STRING)
    @Schema(description = "SNS 타입", nullable = false, example = "KAKAO")
    private val socialType: SocialType,

    @field:NotBlank
    @Column(nullable = false, updatable = false, unique = true)
    @Schema(description = "SNS 사용자 번호", nullable = false, example = "1234567")
    private val socialUserNumber: String
)
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private val id: Long = 0L
    enum class SocialType {
        KAKAO, GOOGLE, APPLE
    }
}
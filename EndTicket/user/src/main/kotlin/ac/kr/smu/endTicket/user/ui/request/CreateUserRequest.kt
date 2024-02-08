package ac.kr.smu.endTicket.user.ui.request

import ac.kr.smu.endTicket.user.domain.model.User
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size

/**
 * 사용자 생성 요청을 추상화한 클래스
 * @property nickname 사용자 별명
 * @property email 사용자 이메일
 * @property socialType 생성 요청을 한 SNS 타입
 * @property code SNS OAuth에서 인증하고 응답받은 authorization code
 */
@Schema(description = "사용자 생성 요청")
data class CreateUserRequest(
    @field:Size(min = 3, max = 8)
    @Schema(description = "별명", nullable = false, example = "테스트")
    val nickname: String,

    @field:Email
    @Schema(description = "이메일", nullable = false, example = "test@test.com")
    val email: String,

    @field:NotNull
    @Schema(description = "SNS 타입", nullable = false, example = "KAKAO")
    val socialType: User.SocialType,

    @field:NotBlank
    @Schema(description = "SNS 인증 코드", nullable = false, example = "LTXdE0-xf2CuXua_M4YFGbS6gThxAlVpwq-drntxv1nkyN7ztxVJy_pPMLkKPXLrAAABjYJsmeWm1x-HnlkNwQ")
    val code: String
)

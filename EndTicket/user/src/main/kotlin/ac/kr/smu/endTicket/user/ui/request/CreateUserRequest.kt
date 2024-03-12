package ac.kr.smu.endTicket.user.ui.request

import ac.kr.smu.endTicket.user.domain.model.User
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size

/**
 * 사용자 생성 요청을 추상화한 클래스
 * @property socialType 생성 요청을 한 SNS 타입
 * @property socialUserNumber SNS 사용자 번호
 */
@Schema(description = "사용자 생성 요청")
data class CreateUserRequest(
    @field:NotNull(message = "SNS 타입은 null일 수 없습니다.")
    @Schema(description = "SNS 타입", nullable = false, example = "KAKAO")
    val socialType: User.SocialType,

    @field:NotBlank(message = "SNS 사용자 번호는 null일 수 없습니다.")
    @Schema(description = "SNS 사용자 번호", nullable = false, example = "1234152")
    val socialUserNumber: String
)

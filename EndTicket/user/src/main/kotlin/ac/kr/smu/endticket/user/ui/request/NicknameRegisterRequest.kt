package ac.kr.smu.endticket.user.ui.request

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size

/**
 * 닉네임 등록 요청 입력에 대한 클래스
 * @param nickname 요청으로 받은 닉네임
 */
data class NicknameRegisterRequest(
    @field:NotEmpty(message = "닉네임은 비어있을 수 없습니다.")
    @field:Size(min = 3, max = 8, message = "닉네임의 길이는 3자 이상 8자 이하여야 합니다.")
    @field:Pattern(regexp = "[a-z|A-Z|ㄱ-ㅎ|가-힣|]+", message = "닉네임은 특수문자 및 공백을 허용하지 않습니다.")
    @Schema(description = "등록할 닉네임", example = "nickname")
    val nickname: String,
)

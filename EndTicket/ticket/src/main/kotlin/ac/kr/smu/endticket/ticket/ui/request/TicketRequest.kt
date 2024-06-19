package ac.kr.smu.endticket.ticket.ui.request

import ac.kr.smu.endticket.ticket.domain.model.Ticket
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size

/**
 * 티켓 생성 혹은 수정 요청에 사용되는 객체
 * @property behavior 행동
 * @property target 목표
 * @property color 티켓의 색
 * @property type 티켓의 분류
 * @property maxSwipeCount 최대 스와이프 횟ㄹ
 */

@Schema(description = "티켓 생성 혹은 수정 요청")
data class TicketRequest(
    @field:NotBlank(message = "공백일 수 없습니다.")
    @field:Size(max = 20, message = "20자 이내로 작성해주세요.")
    @Schema(description = "행동", example = "힘들어도 눈치 보지 말고 꼭 대화하기")
    val behavior: String,

    @field:NotBlank(message = "공백일 수 없습니다.")
    @field:Size(max = 20, message = "20자 이내로 작성해주세요.")
    @Schema(description = "목표", example = "많은 사람들 앞에서 당당한 내 모습")
    val target: String,

    @field:NotNull
    @Schema(description = "티켓의 색", implementation = Ticket.Color::class)
    val color: Ticket.Color,

    @field:NotNull
    @Schema(description = "분류", implementation = Ticket.Type::class)
    val type: Ticket.Type,

    @field:NotNull
    @Schema(description = "최대 스와이프 횟수", implementation = Ticket.MaxSwipeCount::class)
    val maxSwipeCount: Ticket.MaxSwipeCount
)

package ac.kr.smu.endTicket.ticket.ui.request

import ac.kr.smu.endTicket.ticket.domain.model.Ticket
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull

/**
 * 티켓 생성 혹은 수정에 사용되는 객체
 * @param behavior 행동
 * @param target 목표
 * @param color 티켓의 색
 * @param type 티켓의 분류
 * @param swipeCount 티켓의 스와이프 개수
 */

@Schema(description = "티켓")
data class TicketRequest(
    @field:NotBlank
    @Schema(description = "행동", example = "힘들어도 눈치 보지 말고 꼭 대화하기")
    val behavior: String,

    @field:NotBlank
    @Schema(description = "목적", example = "많은 사람들 앞에서 당당한 내 모습")
    val target: String,

    @field:NotNull
    @Schema(description = "티켓의 색", implementation = Ticket.Color::class)
    val color: Ticket.Color,

    @field:NotNull
    @Schema(description = "분류", implementation = Ticket.Type::class)
    val type: Ticket.Type,

    @field:NotNull
    @Schema(description = "스와이프 횟수", implementation = Ticket.SwipeCount::class)
    val swipeCount: Ticket.SwipeCount,
)

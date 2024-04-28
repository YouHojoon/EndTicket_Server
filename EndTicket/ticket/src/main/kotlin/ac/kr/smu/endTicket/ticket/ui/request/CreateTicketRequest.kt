package ac.kr.smu.endTicket.ticket.ui.request

import ac.kr.smu.endTicket.ticket.domain.model.Ticket
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.persistence.Column
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull

data class CreateTicketRequest(
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

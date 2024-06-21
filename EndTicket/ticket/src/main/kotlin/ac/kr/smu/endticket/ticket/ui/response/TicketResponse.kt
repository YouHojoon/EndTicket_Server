package ac.kr.smu.endticket.ticket.ui.response

import ac.kr.smu.endticket.common.web.enum.Color
import ac.kr.smu.endticket.common.web.enum.TicketType
import ac.kr.smu.endticket.ticket.domain.model.Ticket
import io.swagger.v3.oas.annotations.media.Schema

/**
 * 티켓을 반환해줄 때 사용하는 객체
 * @property id 티켓의 ID
 * @property behavior 행동
 * @property target 목표
 * @property color 티켓의 색깔
 * @property type 분류
 * @property swipeCount 현재 스와이프 횟수
 * @property maxSwipeCount 최대 스와이프 횟수
 */
@Schema(description = "티켓에 대한 응답")
data class TicketResponse(
    @Schema(description = "티켓의 ID", example = "1")
    val id: Long,

    @Schema(description = "행동", example = "힘들어도 눈치 보지 말고 꼭 대화하기")
    val behavior: String,

    @Schema(description = "목표", example = "많은 사람들 앞에서 당당한 내 모습")
    val target: String,

    @Schema(description = "티켓의 색", implementation = Color::class)
    val color: Color,

    @Schema(description = "분류", implementation = TicketType::class)
    val type: TicketType,

    @Schema(description = "현재 스와이프 횟수", example = "0")
    val swipeCount: Int,

    @Schema(description = "최대 스와이프 횟수", implementation = Ticket.MaxSwipeCount::class)
    val maxSwipeCount: Ticket.MaxSwipeCount
)
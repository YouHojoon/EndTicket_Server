package ac.kr.smu.endticket.history.ui.response

import ac.kr.smu.endticket.common.web.enum.Color
import ac.kr.smu.endticket.common.web.enum.TicketType
import ac.kr.smu.endticket.history.domain.model.TicketHistory
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDateTime

/**
 * 티켓 기록 응답
 * @property behavior 행동
 * @property target 목표
 * @property color 색
 * @property type 분류
 * @property swipeCount 스와이프 횟수
 */
@Schema(description = "티켓 기록 응답")
class TicketHistoryResponse(
    @Schema(description = "행동", example = "힘들어도 눈치 보지 말고 꼭 대화하기")
    val behavior: String,

    @Schema(description = "목표", example = "많은 사람들 앞에서 당당한 내 모습")
    val target: String,

    @Schema(description = "티켓의 색", implementation = Color::class)
    val color: Color,

    @Schema(description = "분류", implementation = TicketType::class)
    val type: TicketType,

    @Schema(description = "스와이프 횟수", example = "5")
    val swipeCount: Int,

    @Schema(description = "완료 일자", example = "2024-06-22T12:53:58.834278")
    completedAt: LocalDateTime
): HistoryResponse(completedAt)
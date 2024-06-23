package ac.kr.smu.endticket.history.ui.response

import io.swagger.v3.oas.annotations.media.Schema

/**
 * 기록 개수들을 나타내는 클래스
 * @property ticketHistoryCount 티켓 기록의 개수
 * @property imaginationHistoryCount 상상해보기 기록의 개수
 */
@Schema(description = "기록 개수들")
data class HistoryCount(
    @Schema(description = "티켓 기록의 개수", example = "1")
    val ticketHistoryCount: Int,
    @Schema(description = "상상해보기 기록의 개수", example = "1")
    val imaginationHistoryCount: Int
)
package ac.kr.smu.endticket.history.ui.response

import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDateTime

/**
 * 기록 응답 추상화 객체
 * @param createdAt 완료 일자
 */
sealed class HistoryResponse(
    @Schema(description = "완료 일자", example = "2024-06-22T12:53:58.834278")
    val createdAt: LocalDateTime,
)

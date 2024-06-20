package ac.kr.smu.endticket.common.web.enum

import io.swagger.v3.oas.annotations.media.Schema

/**
 * 티켓의 분류
 * @property HEALTH 건강
 * @property PERSONALITY 성격
 * @property VALUES 가치관
 * @property SELF_IMPROVEMENT 자기계발
 * @property RELATIONSHIP 관계
 */
@Schema(description = "티켓의 분류")
enum class TicketType {
    @Schema(description = "건강")
    HEALTH,
    @Schema(description = "성격")
    PERSONALITY,
    @Schema(description = "가치관")
    VALUES,
    @Schema(description = "자기계발")
    SELF_IMPROVEMENT,
    @Schema(description = "관계")
    RELATIONSHIP
}

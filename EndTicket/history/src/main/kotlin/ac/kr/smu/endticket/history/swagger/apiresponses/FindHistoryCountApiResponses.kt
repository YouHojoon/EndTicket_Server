package ac.kr.smu.endticket.history.swagger.apiresponses

import ac.kr.smu.endticket.history.ui.response.HistoryCount
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses

@ApiResponses(
    ApiResponse(
        responseCode = "200",
        description = "조회 성공",
        content = [Content(schema = Schema(implementation = HistoryCount::class))]
    )
)
@Operation(description = "기록 개수들 조회", summary = "기록 개수들 조회")
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class FindHistoryCountApiResponses {
}
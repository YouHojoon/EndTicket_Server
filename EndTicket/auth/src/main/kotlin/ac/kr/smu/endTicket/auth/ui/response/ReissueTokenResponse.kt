package ac.kr.smu.endTicket.auth.ui.response

import com.fasterxml.jackson.annotation.JsonInclude
import io.swagger.v3.oas.annotations.media.Schema

/**
 * 토큰 재발급 요청 응답 추상화 클래스
 * @property accessToken access 토큰
 * @property refreshToken refresh 토큰
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "토큰 재발급 요청 응답")
data class ReissueTokenResponse(
    @Schema(description = "access 토큰", example = "eyJhbGciOiJIUzM4NCJ9.eyJpYXQiOjE3MTAwOTAyODMsInN1YiI6IjUiLCJleHAiOjE3MTAwOTAyODN9.lugWtaiZMq6DHTR1xB0OzV67phhyjcgxdpmYbOcTE8BvJRZywuaZmtTujnFfeEEn")
    val accessToken: String,

    @Schema(description = "refresh 토큰", nullable = true, example = "eyJhbGciOiJIUzM4NCJ9.eyJpYXQiOjE3MTAwOTAyODMsImV4cCI6MTcxMDY5NTA4M30.8J-YjwoIW4E8SGEU9WJcceMPvxBggj7AIquWb94_I180AtIig73yp0k4GfscN3s3")
    val refreshToken: String?
)
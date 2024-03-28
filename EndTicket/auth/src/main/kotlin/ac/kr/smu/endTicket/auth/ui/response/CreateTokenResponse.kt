package ac.kr.smu.endTicket.auth.ui.response
import io.swagger.v3.oas.annotations.media.Schema

/**
 * 토큰 생성에 대한 응답을 추상화한 클래스
 * @property accessToken access 토큰
 * @property refreshToken refresh 토큰
 */
@Schema(description = "토큰 생성에 대한 응답")
data class CreateTokenResponse(
    @Schema(description = "access 토큰", example = "eyJhbGciOiJIUzM4NCJ9.eyJpYXQiOjE3MTAwODc0OTEsInN1YiI6IjUiLCJleHAiOjE3MTAwODc0OTF9.3RekaLKzxcJvubOp-6kGKRqzg28FO5Xf7EZpn8fZvTJu2g9nmBS7B20-Q8DWQa6I")
    val accessToken: String,
    @Schema(description = "refresh 토큰", example = "eyJhbGciOiJIUzM4NCJ9.eyJpYXQiOjE3MTAwODc0OTEsInN1YiI6IjUiLCJleHAiOjE3MTA2OTIyOTF9.0MtP7Iq5VQGYdHazDfSI7sIgc9Fbgh_fe4v0PMgcZTsDRCQ03EC6VqHASV7xvRev")
    val refreshToken: String
)
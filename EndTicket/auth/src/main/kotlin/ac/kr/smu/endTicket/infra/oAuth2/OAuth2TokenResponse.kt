package ac.kr.smu.endTicket.infra.oAuth2

import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming


/**
 * OAuth의 응답을 추상화한 클래스
 * @property tokenType bearer로 고정
 * @property accessToken 사용자 액세스 토큰 값
 * @property idToken ID 토큰 값
 * @property expiresIn 만료 시간
 * @property refreshToken 리프레시 토큰 값
 * @property refreshTokenExpiresIn 리프레스 토큰 만료 시간
 * @property scope 정보 조회 권한 범위
 */

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class OAuth2TokenResponse(
    private val tokenType: String,
    private val accessToken: String,
    val idToken: String,
    private val expiresIn: Int,
    private val refreshToken: String?,
    private val refreshTokenExpiresIn:String,
    private val scope: String
)

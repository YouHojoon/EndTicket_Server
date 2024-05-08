package ac.kr.smu.endTicket.auth.infra.oAuth2.idToken

/**
 * ID 토큰의 페이로드를 추상화한 클래스
 * @property iss 발급받은 서비스의 URL
 * @property aud client ID
 * @property sub SNS 사용자 번호
 * @property exp 만료 시간
 * @property iat 발급 시간
 */
data class IDTokenPayload(
    var iss: String = "",
    var aud: String = "",
    var sub: String = "",
    var exp: Long = 0,
    var iat: Long = 0,

    var nonce: String? = null,
    var email: String? = null,
    var auth_time: Long? = null,
    var azp: String? = null,
    var email_verified: Boolean? = null,
    var at_hash: String? = null,
)
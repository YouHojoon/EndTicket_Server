package ac.kr.smu.endticket.auth.domain.exception

/**
 * 리프레시 토큰이 만료되었을때 발생하는 에러
 * @param token 만료된 토큰
 */
class RefreshTokenExpiredException(
    token: String,
) : RuntimeException("$token 은 만료되었습니다.")

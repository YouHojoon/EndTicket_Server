package ac.kr.smu.endTicket.auth.domain.exception

class RefreshTokenExpiredException(
    val token: String
) : RuntimeException("$token 은 만료되었습니다.")
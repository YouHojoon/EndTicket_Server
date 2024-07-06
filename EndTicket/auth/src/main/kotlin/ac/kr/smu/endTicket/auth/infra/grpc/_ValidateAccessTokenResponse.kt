package ac.kr.smu.endTicket.auth.infra.grpc

import ac.kr.smu.endticket.protobuf.ValidateAccessTokenResponse

/**
 * gRPC를 통해 반환될 응답을 생성하는 메소드
 * @param userId 토큰에서 파싱한 사용자 Id
 * @param status 상태, HttpStatusCode와 대응된다.
 * @param message 에러 발생 시 메시지
 */
fun validateAccessTokenResponseOf(
    userId: Long? = null,
    status: Int,
    message: String? = null,
): ValidateAccessTokenResponse {
    val response =
        ValidateAccessTokenResponse
            .newBuilder()
            .setStatus(status)

    response.setUserId(userId ?: -1)
    if (message != null) {
        response.setMessage(message)
    }

    return response.build()
}
package ac.kr.smu.endTicket.auth.domain.model

import io.swagger.v3.oas.annotations.media.Schema

/**
 * 인증에 사용할 SNS
 * KAKAO, GOOGLE, APPLE이 가능하다
 */
@Schema(description = "인증에 사용할 SNS")
enum class SocialType {
    KAKAO, GOOGLE, APPLE
}
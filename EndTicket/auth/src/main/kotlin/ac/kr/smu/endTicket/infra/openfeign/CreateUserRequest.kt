package ac.kr.smu.endTicket.infra.openfeign

import ac.kr.smu.endTicket.auth.domain.model.SocialType

data class CreateUserRequest(val socialUserNumber: String, val socialType: SocialType)
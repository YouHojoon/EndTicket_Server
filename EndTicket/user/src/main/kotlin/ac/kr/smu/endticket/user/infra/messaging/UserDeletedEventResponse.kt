package ac.kr.smu.endticket.user.infra.messaging

/**
 * 사용자 삭제 이벤트 응답
 * @property id 삭제된 사용자의 id
 */
data class UserDeletedEventResponse(
    val id: Long
)
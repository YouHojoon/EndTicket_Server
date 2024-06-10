package ac.kr.smu.endTicket.futureMe.infra.messaging

/**
 * 상상해보기 완료 이벤트 메시지를 나타내는 클래스
 * @property key 메시지의 key
 * @property payload 완료된 이벤트의 응답
 */
data class ImaginationCompletionEventMessage(
    val key: Long,
    val payload: ImaginationCompletionEventResponse
)
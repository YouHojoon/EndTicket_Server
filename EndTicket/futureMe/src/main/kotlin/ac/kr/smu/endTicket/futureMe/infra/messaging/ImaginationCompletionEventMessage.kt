package ac.kr.smu.endTicket.futureMe.infra.messaging

import ac.kr.smu.endTicket.common.kafka.messaging.KafkaMessage

/**
 * 상상해보기 완료 이벤트 메시지를 나타내는 클래스
 * @property key 메시지의 key
 * @property payload 완료된 이벤트의 응답
 */
class ImaginationCompletionEventMessage(
    key: String,
    payload: ImaginationCompletionEventResponse
): KafkaMessage<String, ImaginationCompletionEventResponse>(key,payload)
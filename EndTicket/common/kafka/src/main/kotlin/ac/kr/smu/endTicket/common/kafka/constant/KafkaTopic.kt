package ac.kr.smu.endTicket.common.kafka.constant

import javax.lang.model.type.UnknownTypeException

/**
 * 카프카 토픽의 이름을 저장하는 클래스
 */
enum class KafkaTopic(val topicName: String) {
    TICKET_COMPLETION("endTicket.ticket.completion"), IMAGINATION_COMPLETION("endTicket.imagination.completion");
}
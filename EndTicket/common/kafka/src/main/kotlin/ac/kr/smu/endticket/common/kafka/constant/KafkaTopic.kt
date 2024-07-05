package ac.kr.smu.endticket.common.kafka.constant

/**
 * 카프카 토픽의 이름을 저장하는 클래스
 */
object KafkaTopic {
    const val USER_DELETED = "endticket.user.deleted"
    const val TICKET_COMPLETED = "endticket.ticket.completed"
    const val IMAGINATION_COMPLETED = "endticket.imagination.completed"
}

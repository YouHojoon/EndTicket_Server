package ac.kr.smu.endTicket.futureMe.domain.event.model

import ac.kr.smu.endticket.common.kafka.messaging.KafkaMessage
import ac.kr.smu.endTicket.futureMe.domain.imagination.model.Imagination
import ac.kr.smu.endTicket.futureMe.infra.messaging.ImaginationCompletedEventResponse
import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.JoinColumn
import jakarta.persistence.MapsId
import jakarta.persistence.OneToOne
import jakarta.persistence.PrimaryKeyJoinColumn
import jakarta.persistence.Table
import java.time.LocalDateTime

/**
 * 상상해보기 완료 이벤트
 * @property imagination 완료된 상상해보기
 */
@Entity
@PrimaryKeyJoinColumn(name="id")
@Table
class ImaginationCompletedEvent(
    @OneToOne(fetch = FetchType.LAZY, cascade = [CascadeType.REMOVE])
    @JoinColumn(name = "event_id")
    private val imagination: Imagination,
) : Event(imagination.userID) {
    val imaginationID: Long
        get() = imagination.id
    /**
     * 이벤트 메시지로 변환하는 메소드
     * @return 변환된 메시지
     */
    fun toMessage() = KafkaMessage(
        key = imagination.userID.toString(),
        payload =
        ImaginationCompletedEventResponse(
            id = imagination.id,
            behavior = imagination.behavior,
            target = imagination.target,
            color = imagination.color,
            completedDate = imagination.audit.updatedAt ?: LocalDateTime.now()
        )
    )

    /**
     * 이벤트의 메시지가 발행되었는지 나타내는 필드
     */
    @Column
    var isSent = false
        private set

    /**
     * 이벤트의 메시지 발행 완료 메소드
     */
    fun successSend() {
        isSent = true
    }
}
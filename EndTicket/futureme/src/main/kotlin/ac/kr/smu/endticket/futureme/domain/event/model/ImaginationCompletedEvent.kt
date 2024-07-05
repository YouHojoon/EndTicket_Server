package ac.kr.smu.endticket.futureme.domain.event.model

import ac.kr.smu.endticket.common.kafka.messaging.KafkaMessage
import ac.kr.smu.endticket.common.web.enum.CharacterType
import ac.kr.smu.endticket.futureme.domain.imagination.model.Imagination
import jakarta.persistence.CascadeType
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.JoinColumn
import jakarta.persistence.OneToOne
import jakarta.persistence.PrimaryKeyJoinColumn
import jakarta.persistence.Table

/**
 * 상상해보기 완료 이벤트
 * @property imagination 완료된 상상해보기
 */
@Entity
@PrimaryKeyJoinColumn(name = "id")
@Table
class ImaginationCompletedEvent(
    @OneToOne(fetch = FetchType.LAZY, cascade = [CascadeType.REMOVE])
    @JoinColumn(name = "imagination_id")
    private val imagination: Imagination,
) : Event(imagination.userId) {
    val imaginationId: Long
        get() = imagination.id

    /**
     * 이벤트 메시지로 변환하는 메소드
     * @return 변환된 메시지
     */
    fun toMessage(characterType: CharacterType) =
        KafkaMessage(
            key = imagination.userId.toString(),
            payload = imagination.toEventResponse(characterType),
        )
}

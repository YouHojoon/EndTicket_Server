package ac.kr.smu.endticket.user.domain.model

import ac.kr.smu.endticket.common.jpa.Audit
import ac.kr.smu.endticket.common.kafka.messaging.KafkaMessage
import jakarta.persistence.*

/**
 * 사용자 삭제 이벤트
 * @property user 삭제된 사용자
 */
@Entity
@Table
class UserDeletedEvent(
    @OneToOne(fetch = FetchType.LAZY, cascade = [CascadeType.REMOVE])
    @JoinColumn(name = "id")
    @MapsId
    private val user: User,
) {
    @Id
    private val id: Long = 0L

    @Embedded
    private val audit = Audit()

    fun toMessage() = KafkaMessage<String, Unit>(id.toString())
}

package ac.kr.smu.endticket.user.domain.model

import ac.kr.smu.endticket.common.jpa.Audit
import ac.kr.smu.endticket.common.kafka.messaging.KafkaMessage
import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Embedded
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.MapsId
import jakarta.persistence.OneToOne
import jakarta.persistence.Table

/**
 * 사용자 삭제 이벤트
 * @property user 삭제된 사용자
 */
@Entity
@Table
class UserDeletedEvent(
    @MapsId("id")
    @OneToOne(cascade = [CascadeType.REMOVE])
    private val user: User
) {
    @Id
    private val id: Long = 0L

    @Embedded
    private val audit = Audit()

    fun toMessage() = KafkaMessage<String,Void>(id.toString())
}
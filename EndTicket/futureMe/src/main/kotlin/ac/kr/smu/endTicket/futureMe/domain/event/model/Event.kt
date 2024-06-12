package ac.kr.smu.endTicket.futureMe.domain.event.model

import ac.kr.smu.endTicket.common.jpa.Audit
import jakarta.persistence.*
import org.springframework.data.jpa.domain.support.AuditingEntityListener

/**
 * 이벤트를 추상화하기 위한 클래스
 * @property eventID 이벤트의 ID, 예를 들어 상상해보기의 ID
 * @property userID 사용자의 ID
 */
@Entity
@Table(
    indexes = [
        Index(name = "idx_event_id", columnList = "event_id")
    ]
)
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "type")
@EntityListeners(AuditingEntityListener::class)
sealed class Event(
    @Column(name = "event_id",nullable = false, updatable = false)
    val eventID: Long,
    @Column(nullable = false, updatable = false)
    val userID: Long
){
    /**
     * event 엔티티의 자체 ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private val id: Long = 0L

    @Embedded
    val audit = Audit()

    @Column(insertable = false, updatable = false)
    private val type = ""
}

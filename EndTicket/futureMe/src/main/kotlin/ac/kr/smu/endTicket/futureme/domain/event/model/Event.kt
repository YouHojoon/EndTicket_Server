package ac.kr.smu.endTicket.futureme.domain.event.model

import ac.kr.smu.endTicket.common.jpa.Audit
import jakarta.persistence.*
import org.hibernate.annotations.DiscriminatorOptions
import org.springframework.data.jpa.domain.support.AuditingEntityListener

/**
 * 이벤트를 추상화하기 위한 클래스
 * @property eventID 이벤트의 Id, 예를 들어 상상해보기의 Id
 * @property userID 사용자의 Id
 */
@Entity
@Table
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorOptions(force = false)
@EntityListeners(AuditingEntityListener::class)
sealed class Event(
    @Column(name = "user_id", nullable = false, updatable = false)
    val userId: Long
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

package ac.kr.smu.endticket.history.domain.model

import ac.kr.smu.endticket.common.jpa.Audit
import ac.kr.smu.endticket.history.ui.response.HistoryResponse
import ac.kr.smu.endticket.history.ui.response.ImaginationHistoryResponse
import jakarta.persistence.*
import org.hibernate.annotations.DiscriminatorOptions
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.LocalDateTime

/**
 * 기록 추상화 클래스
 * @property completedAt 완료 시간
 */
@Entity
@Table(
    indexes = [
        Index(name = "idx_user_id_completed_at", columnList = "user_id, completed_at")
    ]
)
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorOptions(force = false)
@EntityListeners(AuditingEntityListener::class)
abstract class History(
    @Column(name = "completed_at", updatable = false, nullable = false)
    protected val completedAt: LocalDateTime,

    @Column(name = "user_id", updatable = false, nullable = false)
    val userId: Long
){
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private val id: Long = 0L

    @Embedded
    private val audit = Audit()
    abstract fun toResponse(): HistoryResponse
    enum class Type {
        TICKET, IMAGINATION;
        fun toHistoryClass() = when(this){
            TICKET -> TicketHistory::class
            IMAGINATION -> ImaginationHistory::class
        }
    }
}
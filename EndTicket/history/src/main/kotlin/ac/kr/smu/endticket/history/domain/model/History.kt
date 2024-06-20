package ac.kr.smu.endticket.history.domain.model

import jakarta.persistence.*
import org.hibernate.annotations.DiscriminatorOptions
import java.time.LocalDateTime

/**
 * 기록 추상화 클래스
 *
 * @property completedAt 완료 시간
 */
@Entity
@Table(
    indexes = [
        Index(name = "idx_completed_at", columnList = "completed_at"),
        Index(name = "idx_user_id", columnList = "user_id")
    ]
)
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorOptions(force = false)
abstract class History(
    @Column(name = "completed_at", updatable = false, nullable = false)
    private val completedAt: LocalDateTime,

    @Column(name = "user_id", updatable = false, nullable = false)
    private val userId: Long
){
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private val id: Long = 0L
}
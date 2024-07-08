package ac.kr.smu.endticket.history.domain.model

import ac.kr.smu.endticket.common.jpa.Audit
import ac.kr.smu.endticket.history.ui.response.HistoryResponse
import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.persistence.*
import org.hibernate.annotations.DiscriminatorOptions
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.LocalDateTime

/**
 * 기록 추상화 클래스
 * @property completedAt 완료 시간
 * @property userId 사용자 id
 */
@Entity
@Table(
    indexes = [
        Index(name = "idx_user_id_completed_at", columnList = "user_id, completed_at"),
    ],
)
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorOptions(force = false)
@EntityListeners(AuditingEntityListener::class)
abstract class History(
    @Column(name = "completed_at", updatable = false, nullable = false)
    protected val completedAt: LocalDateTime,
    @Column(name = "user_id", updatable = false, nullable = false)
    val userId: Long,
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private val id: Long = 0L

    @Embedded
    private val audit = Audit()

    /**
     * 기록 응답으로 변환하는 메소드
     */
    abstract fun toResponse(): HistoryResponse

    /**
     * 기록의 종류
     * @property TICKET 티켓
     * @property IMAGINATION 상상해보기
     */
    @Schema(description = "기록의 종류")
    enum class Type {
        @JsonProperty("ticket")
        @Schema(description = "티켓")
        TICKET,
        @JsonProperty("imagination")
        @Schema(description = "상상해보기")
        IMAGINATION,
        ;

        fun toHistoryClass() =
            when (this) {
                TICKET -> TicketHistory::class
                IMAGINATION -> ImaginationHistory::class
            }
    }
}

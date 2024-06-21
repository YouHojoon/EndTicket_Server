package ac.kr.smu.endticket.ticket.domain.model

import ac.kr.smu.endticket.common.jpa.Audit
import ac.kr.smu.endticket.common.kafka.messaging.KafkaMessage
import ac.kr.smu.endticket.ticket.ui.response.TicketResponse
import jakarta.persistence.*
import org.springframework.data.jpa.domain.support.AuditingEntityListener

/**
 * 티켓 완료 이벤트를 추상화한 객체
 * @property ticket 완료된 티켓
 * @property isSent 메시지 발행 여부
 */
@Entity
@Table(name = "ticket_completion_event")
@EntityListeners(AuditingEntityListener::class)
class TicketCompletedEvent(
    @OneToOne(fetch = FetchType.LAZY, cascade = [CascadeType.REMOVE, CascadeType.PERSIST])
    @JoinColumn(name = "id")
    @MapsId
    private val ticket: Ticket,
) {

    @Column
    var isSent: Boolean = false

    @Id
    val id: Long = 0L

    @Embedded
    val audit = Audit()
    /**
     * 메시지를 전송하기 위한 응답으로 변환하는 메소드
     */
    fun toMessage() = KafkaMessage(ticket.userId.toString(), ticket.toEventResponse())
    fun successSend(){
        isSent = true
    }
}
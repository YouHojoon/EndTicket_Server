package ac.kr.smu.endTicket.ticket.domain.model

import ac.kr.smu.endTicket.ticket.ui.response.TicketCompletionEventResponse
import ac.kr.smu.endTicket.ticket.ui.response.TicketResponse
import jakarta.persistence.*
import org.hibernate.annotations.Fetch
import java.time.LocalDateTime

/**
 * 티켓 완료 이벤트를 추상화한 객체
 * @property ticket 완료된 티켓
 * @property published 메시지 발행 여부
 */
@Entity
@Table(name = "ticket_completion_event")
class TicketCompletionEvent private constructor(
    @OneToOne(fetch = FetchType.LAZY, cascade = [CascadeType.REMOVE])
    private val ticket: Ticket,

    @Column
    var published: Boolean = false,
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private val id: Long = 0L

    @Column(updatable = false)
    val createAt: LocalDateTime = LocalDateTime.now()

    companion object{
        /**
         * Ticket으로부터 이벤트를 생성하는 메소드
         * @param ticket 이벤트를 발행할 티켓
         */
        fun from(ticket: Ticket) = TicketCompletionEvent(ticket)
    }

    /**
     * 메시지를 전송하기 위한 응답으로 변환하는 메소드
     */
    fun toResponse(): TicketCompletionEventResponse = TicketCompletionEventResponse(ticket.userID, TicketResponse.from(ticket))

    fun successPublish(){
        published = true
    }
}
package ac.kr.smu.endTicket.ticket.domain.model

import ac.kr.smu.endTicket.ticket.ui.response.TicketCompletionEventResponse
import ac.kr.smu.endTicket.ticket.ui.response.TicketResponse
import jakarta.persistence.*
import org.hibernate.annotations.Fetch
import java.time.LocalDateTime


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
        fun from(ticket: Ticket) = TicketCompletionEvent(ticket)
    }

    fun toResponse(): TicketCompletionEventResponse = TicketCompletionEventResponse(ticket.userID, TicketResponse.from(ticket))
    fun successPublish(){
        published = true
    }
}
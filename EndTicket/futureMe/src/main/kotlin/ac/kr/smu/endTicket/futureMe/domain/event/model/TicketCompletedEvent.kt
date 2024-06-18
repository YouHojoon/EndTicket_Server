package ac.kr.smu.endTicket.futureMe.domain.event.model

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.PrimaryKeyJoinColumn
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint


/**
 * 티켓 완료 이벤트
 * @property id 완료된 ticket의 id
 * @property userID ticket의 소유자 id
 */
@Entity
@PrimaryKeyJoinColumn(name="id")
@Table(
    uniqueConstraints = [
        UniqueConstraint(columnNames = ["ticket_id"])
    ]
)
class TicketCompletedEvent(
    @Column(name = "ticket_id", updatable = false)
    private val ticketID: Long,
    userID: Long
): Event(userID)
package ac.kr.smu.endTicket.futureMe.domain.event

import ac.kr.smu.endTicket.futureMe.domain.imagination.model.Imagination
import ac.kr.smu.endTicket.futureMe.ui.response.ImaginationCompletionEventMessage
import ac.kr.smu.endTicket.futureMe.ui.response.ImaginationCompletionEventResponse
import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.OneToOne
import java.time.LocalDateTime

/**
 * 상상해보기 완료 이벤트
 * @param id 상상해보기 id
 * @param userID 사용자의 id
 */
@Entity
class ImaginationCompletionEvent(
    @OneToOne(fetch = FetchType.LAZY, cascade = [CascadeType.REMOVE])
    private val imagination: Imagination,
) : Event(imagination.id, imagination.userID){
    fun toMessage() = ImaginationCompletionEventMessage(
        key = imagination.userID,
        payload =
        ImaginationCompletionEventResponse(
            behavior = imagination.behavior,
            target = imagination.target,
            color = imagination.color,
            completedDate = imagination.audit.updatedAt ?: LocalDateTime.now()
        )
    )

    @Column
    private var isSent = false

    fun successSend(){
        isSent = true
    }
}
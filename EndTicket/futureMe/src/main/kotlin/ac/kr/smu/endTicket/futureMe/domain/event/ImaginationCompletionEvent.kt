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
 * @property imagination 완료된 상상해보기
 */
@Entity
class ImaginationCompletionEvent(
    @OneToOne(fetch = FetchType.LAZY, cascade = [CascadeType.REMOVE])
    private val imagination: Imagination,
) : Event(imagination.id, imagination.userID){

    /**
     * 이벤트 메시지로 변환하는 메소드
     */
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

    /**
     * 이벤트의 메시지가 발행되었는지 나타내는 필드
     */
    @Column
    private var isSent = false

    /**
     * 이벤트의 메시지 발행 완료 메소드
     */
    fun successSend(){
        isSent = true
    }
}
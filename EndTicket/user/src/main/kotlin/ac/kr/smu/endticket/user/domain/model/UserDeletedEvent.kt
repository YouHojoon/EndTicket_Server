package ac.kr.smu.endticket.user.domain.model

import ac.kr.smu.endticket.common.jpa.Audit
import ac.kr.smu.endticket.user.infra.messaging.UserDeletedEventResponse
import jakarta.persistence.Column
import jakarta.persistence.Embedded
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.MapsId
import jakarta.persistence.OneToOne
import jakarta.persistence.Table

/**
 * 사용자 삭제 이벤트
 * @property user 삭제된 사용자
 */
@Entity
@Table
class UserDeletedEvent(
    @MapsId("id")
    @OneToOne
    private val user: User
) {
    @Id
    private val id: Long = 0L

    @Column
    private var isSent = false

    @Embedded
    private val audit = Audit()

    /**
     * 메시지 전송 성공 메소드
     */
    fun sendSuccess(){
        isSent = true
    }

    /**
     * 메시지 응답으로 반환하는 메소드
     * @return 회원 삭제 이벤트 메시지 응답
     */
    fun toResponse() = UserDeletedEventResponse(id)
}
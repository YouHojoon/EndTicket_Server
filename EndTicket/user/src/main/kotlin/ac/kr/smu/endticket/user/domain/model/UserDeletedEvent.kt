package ac.kr.smu.endticket.user.domain.model

import ac.kr.smu.endticket.common.jpa.Audit
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
    val id: Long = 0L

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
}
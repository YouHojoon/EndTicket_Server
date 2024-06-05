package ac.kr.smu.endTicket.common.jpa

import jakarta.persistence.Embeddable
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import java.time.LocalDateTime

/**
 * 생성일자와 수정일자를 나태내기 위한 클래스
 */
@Embeddable
class Audit{
    @CreatedDate
    var createdAt: LocalDateTime = LocalDateTime.MIN
        private set
    @LastModifiedDate
    var updatedAt: LocalDateTime? = null
        private set
}
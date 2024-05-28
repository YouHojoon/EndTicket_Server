import jakarta.persistence.Embeddable
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import java.time.LocalDateTime

@Embeddable
class Audit{
    @CreatedDate
    var createdAt: LocalDateTime = LocalDateTime.MIN
        private set
    @LastModifiedDate
    var updatedAt: LocalDateTime? = null
        private set
}
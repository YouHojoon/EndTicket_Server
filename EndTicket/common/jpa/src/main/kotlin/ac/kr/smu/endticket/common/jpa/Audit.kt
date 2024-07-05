package ac.kr.smu.endticket.common.jpa

import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer
import jakarta.persistence.Column
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
    @JsonSerialize(using = LocalDateTimeSerializer::class)
    @JsonDeserialize(using = LocalDateTimeDeserializer::class)
    @Column(name = "created_at")
    var createdAt: LocalDateTime = LocalDateTime.now()
        protected set
    @LastModifiedDate
    @JsonSerialize(using = LocalDateTimeSerializer::class)
    @JsonDeserialize(using = LocalDateTimeDeserializer::class)
    @Column(name = "updated_at")
    var updatedAt: LocalDateTime? = null
        protected set
}
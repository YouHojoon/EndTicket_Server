package ac.kr.smu.endticket.common.jpa

import jakarta.persistence.*
import org.springframework.data.jpa.domain.support.AuditingEntityListener

@Entity
@Table
@EntityListeners(AuditingEntityListener::class)
class MockDomain(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
     val id: Long = 0L,

    @Column
    var variable: Int = 0,

){
   @Embedded
   val audit: Audit = Audit()
}
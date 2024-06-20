package ac.kr.smu.endticket.history.domain.model

import jakarta.persistence.*
import org.hibernate.annotations.DiscriminatorOptions

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorOptions(force = false)
@Table
sealed class History{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private val id: Long = 0L
}
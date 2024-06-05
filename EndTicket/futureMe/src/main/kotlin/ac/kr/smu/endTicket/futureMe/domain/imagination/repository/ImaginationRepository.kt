package ac.kr.smu.endTicket.futureMe.domain.imagination.repository

import ac.kr.smu.endTicket.futureMe.domain.imagination.model.Imagination
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ImaginationRepository: JpaRepository<Imagination, Long>{
    fun countById(long: Long): Int
}
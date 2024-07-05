package ac.kr.smu.endticket.futureme.domain.futureme.repository

import ac.kr.smu.endticket.futureme.domain.futureme.model.FutureMe
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface FutureMeRepository : JpaRepository<FutureMe, Long>

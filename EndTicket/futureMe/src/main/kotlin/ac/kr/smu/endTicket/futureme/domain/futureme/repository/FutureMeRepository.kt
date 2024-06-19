package ac.kr.smu.endTicket.futureme.domain.futureme.repository

import ac.kr.smu.endTicket.futureme.domain.futureme.model.FutureMe
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface FutureMeRepository: JpaRepository<FutureMe, Long>
package ac.kr.smu.endTicket.futureMe.domain.futureMe.repository

import ac.kr.smu.endTicket.futureMe.domain.futureMe.model.FutureMe
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface FutureMeRepository: JpaRepository<FutureMe, Long>
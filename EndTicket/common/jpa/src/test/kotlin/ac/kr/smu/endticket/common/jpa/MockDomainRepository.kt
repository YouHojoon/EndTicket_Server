package ac.kr.smu.endticket.common.jpa

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface MockDomainRepository : JpaRepository<MockDomain, Long>
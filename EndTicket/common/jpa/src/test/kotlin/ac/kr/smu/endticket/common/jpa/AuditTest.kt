package ac.kr.smu.endticket.common.jpa

import jakarta.persistence.EntityManager
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.context.annotation.ComponentScan
import org.springframework.data.jpa.repository.config.EnableJpaAuditing
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.data.repository.findByIdOrNull
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import kotlin.jvm.optionals.getOrNull
import kotlin.test.assertNotNull
import kotlin.test.assertNull


@DataJpaTest
@SpringJUnitConfig(TestConfiguration::class)
@EnableJpaAuditing(modifyOnCreate = false)
class AuditTest @Autowired constructor(
    private val repo: MockDomainRepository,
    private val em: EntityManager
) {
    @Test
    @DisplayName("audit 테스트")
    fun given_domain_whenSave_then_saveWithAudit(){
        val threadSleepTime = 3000L
        var domain = repo.save(MockDomain())
        assert(domain.audit.createdAt.isBefore(LocalDateTime.now()))
        assertNull(domain.audit.updatedAt)

        Thread.sleep(threadSleepTime)
        domain.variable++
        em.flush()

        val updatedAt = domain.audit.updatedAt
        assertNotNull(updatedAt)
        assert(updatedAt.isAfter(domain.audit.createdAt.plus(threadSleepTime,ChronoUnit.MILLIS)))
    }
}
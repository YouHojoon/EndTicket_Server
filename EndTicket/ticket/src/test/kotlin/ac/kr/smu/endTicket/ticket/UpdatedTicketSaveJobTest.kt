package ac.kr.smu.endTicket.ticket

import ac.kr.smu.endTicket.ticket.job.UpdatedTicketSaveJob
import ac.kr.smu.endTicket.ticket.domain.repository.TicketRepository
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.core.ValueOperations
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootTest(
    properties = [
        "schedules.save-updated-ticket.initialDelay=250",
        "schedules.save-updated-ticket.fixedDelay=100"
    ],
    classes = [UpdatedTicketSaveJob::class]
)
@EnableScheduling
class UpdatedTicketSaveJobTest @Autowired constructor(
    @MockBean
    private val ops: ValueOperations<String, Any>,
    @MockBean
    private val redisTemplate: RedisTemplate<String, Any>,
    @MockBean
    private val repo: TicketRepository,
) {

    @Test
    @DisplayName("Write Back 패턴 테스트")
    fun after_fixedDelay_then_runSaveUpdatedTicket(){
        Mockito.`when`(redisTemplate.opsForValue()).thenReturn(ops)
        Thread.sleep( 300)
        Mockito.verify(repo, Mockito.atLeast(1)).saveAll(Mockito.anyList())
    }
}
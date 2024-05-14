package ac.kr.smu.endTicket

import ac.kr.smu.endTicket.ticket.domain.repository.TicketRepository
import ac.kr.smu.endTicket.ticket.service.TicketService
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
        "schedules.save-updatedTicket-toDB.initialDelay=50",
        "schedules.save-updatedTicket-toDB.fixedDelay=100"
    ],
    classes = [TicketService::class]
)
@EnableScheduling
class SaveUpdatedTicketJobTest @Autowired constructor(
    @MockBean
    private val ops: ValueOperations<String, Any>,
    @MockBean
    private val repo: TicketRepository,
    @MockBean
    private val redisTemplate: RedisTemplate<String, Any>,

    private val service: TicketService
) {

    @Test
    @DisplayName("Write Back 패턴 테스트")
    fun after_fixedDelay_then_runSaveUpdatedTicketToDB(){
        Mockito.`when`(redisTemplate.opsForValue())
            .thenReturn(ops)

        Thread.sleep( 300)
        Mockito.verify(repo, Mockito.atLeast(2)).saveAll(Mockito.anyList())
    }
}
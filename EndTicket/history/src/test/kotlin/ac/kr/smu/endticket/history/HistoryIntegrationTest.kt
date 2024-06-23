package ac.kr.smu.endticket.history

import ac.kr.smu.endticket.history.domain.converter.HistoryTypeConverter
import ac.kr.smu.endticket.history.domain.model.History
import ac.kr.smu.endticket.history.domain.model.ImaginationHistory
import ac.kr.smu.endticket.history.domain.model.TicketHistory
import ac.kr.smu.endticket.history.domain.repository.HistoryRepository
import ac.kr.smu.endticket.history.service.HistoryService
import ac.kr.smu.endticket.history.ui.controller.HistoryController
import org.hibernate.dialect.function.NvlCoalesceEmulation
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration
import org.springframework.boot.autoconfigure.transaction.TransactionAutoConfiguration
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.data.web.PageableHandlerMethodArgumentResolver
import org.springframework.format.support.FormattingConversionService
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import kotlin.test.AfterTest
import kotlin.test.BeforeTest

@SpringBootTest(
    classes = [
        HistoryController::class,
        HistoryService::class,
        DataSourceAutoConfiguration::class,
        TransactionAutoConfiguration::class,
        HibernateJpaAutoConfiguration::class
    ]
)
@EnableJpaRepositories("ac.kr.smu.endticket.history.domain.repository")
@EntityScan("ac.kr.smu.endticket.history.domain.model")
class HistoryIntegrationTest @Autowired constructor(
    controller: HistoryController,
    private val repo: HistoryRepository,
) {
    private val mvc: MockMvc =
        MockMvcBuilders
        .standaloneSetup(controller)
        .setCustomArgumentResolvers(PageableHandlerMethodArgumentResolver())
        .setConversionService(FormattingConversionService().also { it.addConverter(HistoryTypeConverter()) })
            .build()

    @AfterTest
    fun reset(){
        repo.deleteAll()
    }

    @ParameterizedTest
    @DisplayName("기록 조회 테스트")
    @MethodSource("${HistoryTestParameters.PATH}#provideHistoriesAndType")
    fun given_specificIdAndType_when_findHistories_then_returnHistories(histories: Set<out History>, type: History.Type){
        repo.saveAll(histories)

        mvc.findHistories(type)
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.jsonPath("histories").isArray)
            .andExpect(MockMvcResultMatchers.jsonPath("histories").isNotEmpty)
    }
    @ParameterizedTest
    @DisplayName("기록 개수 조회 테스트")
    @MethodSource("${HistoryTestParameters.PATH}#provideHistoriesOfEachType")
    fun given_userId_when_findHistoryCount_then_returnHistoryCount(ticketHistories: Set<TicketHistory>, imaginationHistories: Set<ImaginationHistory>){
        repo.saveAll(ticketHistories)
        repo.saveAll(imaginationHistories)

        mvc.findHistoryCount()
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.jsonPath("ticketHistoryCount").value(ticketHistories.size))
            .andExpect(MockMvcResultMatchers.jsonPath("imaginationHistoryCount").value(imaginationHistories.size))
    }
}
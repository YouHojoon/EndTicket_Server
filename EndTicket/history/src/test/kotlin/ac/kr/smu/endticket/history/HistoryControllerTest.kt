package ac.kr.smu.endticket.history

import ac.kr.smu.endticket.common.redis.test.RedisTestConfig
import ac.kr.smu.endticket.history.domain.model.History
import ac.kr.smu.endticket.history.service.HistoryService
import ac.kr.smu.endticket.history.ui.controller.HistoryController
import ac.kr.smu.endticket.history.ui.response.HistoryCount
import ac.kr.smu.endticket.history.ui.response.HistorySlice
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.context.annotation.Import
import org.springframework.data.domain.PageRequest
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.result.MockMvcResultMatchers

@Import(RedisTestConfig::class)
@WebMvcTest(controllers = [HistoryController::class])
@AutoConfigureMockMvc(addFilters = false)
@MockBean(JpaMetamodelMappingContext::class)
class HistoryControllerTest
    @Autowired
    constructor(
        @MockBean
        private val service: HistoryService,
        private val mvc: MockMvc,
    ) {
        @ParameterizedTest
        @DisplayName("기록 조회 테스트")
        @MethodSource("${HistoryTestParameters.PATH}#provideHistoriesAndType")
        fun given_specificIdAndType_when_findHistories_then_returnHistories(
            histories: Set<History>,
            type: History.Type,
        ) {
            val pageable = PageRequest.of(0, 10)
            Mockito
                .`when`(service.findHistories(HistoryTestParameters.USER_ID, type, pageable))
                .thenReturn(HistorySlice(histories.map(History::toResponse), pageable))

            mvc
                .findHistories(type)
                .andExpect(MockMvcResultMatchers.status().isOk)
                .andExpect(MockMvcResultMatchers.jsonPath("histories").isArray)
                .andExpect(MockMvcResultMatchers.jsonPath("histories").isNotEmpty)

            Mockito.verify(service).findHistories(HistoryTestParameters.USER_ID, type, pageable)
        }

        @Test
        @DisplayName("기록 개수 조회 테스트")
        fun given_userId_when_findHistoryCount_then_returnHistoryCount() {
            Mockito
                .`when`(service.findHistoryCount(HistoryTestParameters.USER_ID))
                .thenReturn(HistoryCount(1, 0, 1))

            mvc
                .findHistoryCount()
                .andExpect(MockMvcResultMatchers.status().isOk)
                .andExpect(MockMvcResultMatchers.jsonPath("ticketHistoryCount").isNumber)
                .andExpect(MockMvcResultMatchers.jsonPath("imaginationHistoryCount").isNumber)
                .andExpect(MockMvcResultMatchers.jsonPath("ticketSwipeCount").isNumber)
        }
    }

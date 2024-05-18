package ac.kr.smu.endTicket

import ac.kr.smu.endTicket.ticket.domain.model.Ticket
import ac.kr.smu.endTicket.ticket.ui.controller.TicketController
import ac.kr.smu.endTicket.ticket.ui.request.TicketRequest
import ac.kr.smu.endTicket.aop.BindExceptionAdvice
import ac.kr.smu.endTicket.constant.HttpHeaderName
import ac.kr.smu.endTicket.constant.KafkaTopic
import ac.kr.smu.endTicket.test.RedisTestConfig
import ac.kr.smu.endTicket.test.andReturn
import ac.kr.smu.endTicket.test.createKafkaContainer
import ac.kr.smu.endTicket.test.messageListener
import ac.kr.smu.endTicket.ticket.domain.repository.TicketCompletionEventRepository
import ac.kr.smu.endTicket.ticket.domain.repository.TicketRepository
import ac.kr.smu.endTicket.ticket.ui.response.TicketResponse
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.http.MediaType
import org.springframework.kafka.listener.KafkaMessageListenerContainer
import org.springframework.kafka.test.EmbeddedKafkaBroker
import org.springframework.kafka.test.context.EmbeddedKafka
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.ResultActions
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.TimeUnit
import kotlin.test.assertEquals
import kotlin.test.assertNotNull


@EmbeddedKafka(
    partitions = 3,
    ports = [9292],
    brokerProperties = [
        "listeners=PLAINTEXT://localhost:9292"
    ]
)
@SpringBootTest
@Import(RedisTestConfig::class)
class TicketIntegrationTest @Autowired constructor(
    private val controller: TicketController,
    private val redisTemplate: RedisTemplate<String, Any>,
    private val ticketRepository: TicketRepository,
    private val eventRepository: TicketCompletionEventRepository,
    private val broker: EmbeddedKafkaBroker
) {
    private val mvc: MockMvc =
        MockMvcBuilders.standaloneSetup(controller)
            .setControllerAdvice(BindExceptionAdvice())
            .build()
    private lateinit var container: KafkaMessageListenerContainer<String, TicketResponse>
    private companion object{
        private const val BASE_URI = "http://localhost:8082/tickets"
    }

    @AfterEach
    fun reset(){
        val connection = redisTemplate.connectionFactory?.connection ?: return
        connection.serverCommands().flushAll()

        eventRepository.deleteAll()
        ticketRepository.deleteAll()
    }

    @Test
    @DisplayName("티켓 생성 테스트")
    fun given_ticketRequest_when_createTicket_then_expectStatusCode204_and_responseCreatedTicket(){
        createTicketRequest(TICKET_REQUEST)
            .andExpect(MockMvcResultMatchers.status().isCreated)
            .andExpect(MockMvcResultMatchers.jsonPath("behavior").value(TICKET_REQUEST.behavior))
            .andExpect(MockMvcResultMatchers.jsonPath("target").value(TICKET_REQUEST.target))
            .andExpect(MockMvcResultMatchers.jsonPath("color").value(TICKET_REQUEST.color.name))
            .andExpect(MockMvcResultMatchers.jsonPath("type").value(TICKET_REQUEST.type.name))
            .andExpect(MockMvcResultMatchers.jsonPath("maxSwipeCount").value(TICKET_REQUEST.maxSwipeCount.name))
            .andExpect(MockMvcResultMatchers.jsonPath("swipeCount").value(0))
    }

    @Test
    @DisplayName("비정상적인 티켓 생성 테스트")
    fun given_invalidTicketRequest_when_createTicket_then_expectStatusCode400(){
        val request = TicketRequest(
            behavior = "",
            target = "",
            color = Ticket.Color.RED1,
            type = Ticket.Type.HEALTH,
            maxSwipeCount = Ticket.MaxSwipeCount.FIVE
        )

        createTicketRequest(request).expectBindingException()
    }

    @Test
    @DisplayName("티캣 개수 제한 이상으로 생성 테스트")
    fun given_userHasReachedTicketLimit_when_createTicket_then_expectStatusCode409(){
        repeat(5){
            createTicketRequest(TICKET_REQUEST)
        }

        createTicketRequest(TICKET_REQUEST)
            .andExpect(MockMvcResultMatchers.status().isConflict)
            .andExpect(MockMvcResultMatchers.jsonPath("code").value(409))
            .andExpect(MockMvcResultMatchers.jsonPath("message").isString)
            .andExpect(MockMvcResultMatchers.jsonPath("detail").isString)
    }

    @Test
    @DisplayName("티켓 수정 테스트")
    fun given_ticketRequest_when_updateTicket_then_expectStatusCode200_and_responseUpdatedTicket(){
        val ticket =
            createTicketRequest(TICKET_REQUEST)
                .andReturn<TicketResponse>()

        updateTicketRequest(UPDATE_REQUEST, ticket.id)
            .andExpect(MockMvcResultMatchers.status().isOk)
    }

    @Test
    @DisplayName("비정상적인 티켓 수정 요청 테스트")
    fun given_invalidTicketRequest_when_updateTicket_then_expectStatusCode400(){
        val ticket =
            createTicketRequest(TICKET_REQUEST)
                .andReturn<TicketResponse>()

        val updateRequest = TicketRequest(
            "",
            "",
            ticket.color,
            ticket.type,
            ticket.maxSwipeCount
        )

        updateTicketRequest(updateRequest,ticket.id).expectBindingException()
    }

    @Test
    @DisplayName("존재하지 않는 티켓 수정 요청 테스트")
    fun given_notExistTicket_when_updateTicket_then_expectStatusCode404(){
        updateTicketRequest(TICKET_REQUEST, 1L).andExpect(MockMvcResultMatchers.status().isNotFound)
    }

    @Test
    @DisplayName("티켓의 소유자가 아닌 사용자의 티켓 수정 요청 테스트")
    fun given_userWhoNotOwnerOfTicket_when_updateTicket_then_expectStatusCode403(){
       val ticket = createTicketRequest(TICKET_REQUEST, 2L).andReturn<TicketResponse>()

        updateTicketRequest(TICKET_REQUEST, ticket.id)
            .andExpect(MockMvcResultMatchers.status().isForbidden)
    }
    @Test
    @DisplayName("티켓 스와이프 테스트")
    fun given_ID_when_swipeTicket_then_responseSwipedTicket(){
        val ticket = createTicketRequest(TICKET_REQUEST).andReturn<TicketResponse>()

        swipeTicketRequest(ticket.id)
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.jsonPath("swipeCount").value(ticket.swipeCount + 1))
    }

    @Test
    @DisplayName("존재하지 않는 티켓 스와이프 테스트")
    fun given_notExistTicket_when_swipeTicket_then_expectStatusCode404(){
        swipeTicketRequest(1L)
            .andExpect(MockMvcResultMatchers.status().isNotFound)
    }

    @Test
    @DisplayName("티켓의 소유자가 아닌 사용자의 스와이프 테스트")
    fun given_userWhoNotOwnerOfTicket_when_swipeTicket_then_expectStatusCode403(){
        val ticket = createTicketRequest(TICKET_REQUEST, 2L).andReturn<TicketResponse>()

        swipeTicketRequest(ticket.id)
            .andExpect(MockMvcResultMatchers.status().isForbidden)
    }

    @Test
    @DisplayName("티켓 스와이프 취소 테스트")
    fun given_ID_when_cancelSwipeTicket_then_responseSwipeCanceledTicket(){
        val ticket = createTicketRequest(TICKET_REQUEST).andReturn<TicketResponse>()
        val beforeSwipeCount = swipeTicketRequest(ticket.id).andReturn<TicketResponse>().swipeCount

        cancelSwipeTicketRequest(ticket.id)
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.jsonPath("swipeCount").value(beforeSwipeCount - 1))
    }

    @Test
    @DisplayName("존재하지 않는 티켓 스와이프 취소 테스트")
    fun given_notExistTicket_when_cancelSwipeTicket_then_expectStatusCode404(){
        cancelSwipeTicketRequest(1L)
            .andExpect(MockMvcResultMatchers.status().isNotFound)
    }

    @Test
    @DisplayName("소유자가 아닌 사용자 티켓 스와이프 취소 테스트")
    fun given_userWhoNotOwnerOfTicket_when_cancelSwipeTicket_then_expectStatusCode403(){
        val ticket = createTicketRequest(TICKET_REQUEST, 2L).andReturn<TicketResponse>()

        cancelSwipeTicketRequest(ticket.id)
            .andExpect(MockMvcResultMatchers.status().isForbidden)
    }

    @Test
    @DisplayName("미완료된 티켓 조회")
    fun given_userID_when_findIncompleteTickets_then_responseIncompleteTickets(){
       val ticket = createTicketRequest(TICKET_REQUEST).andReturn<TicketResponse>()

        val json = mvc.perform(
            MockMvcRequestBuilders
                .get(BASE_URI)
                .header(HttpHeaderName.USER_ID, USER_ID)
        )

            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.jsonPath("tickets").isArray)
            .andReturn<Map<String, List<TicketResponse>>>()

        val tickets = json["tickets"]

        assertNotNull(tickets)
        assert(tickets.contains(ticket))
    }

    @Test
    @DisplayName("티켓 완료 테스트")
    fun given_ticketWhichRightBeforeCompletion_when_swipeTicket_then_responseCompleteTicket_and_sendTicketCompletionEvent(){
        val ticket = createTicketRequest(TICKET_REQUEST).andReturn<TicketResponse>()

        repeat(ticket.maxSwipeCount.value - 1){
            swipeTicketRequest(ticket.id)
        }

        val completeTicket = swipeTicketRequest(ticket.id)
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.jsonPath("swipeCount").value(ticket.maxSwipeCount.value))
            .andReturn<TicketResponse>()

        val queue = LinkedBlockingQueue<TicketResponse>()

        container = createKafkaContainer(broker, KafkaTopic.TICKET_COMPLETION)
        container.messageListener(broker){
            queue.add(it.value())
        }

        val messageResponse = queue.poll(500, TimeUnit.MILLISECONDS)
        assertNotNull(messageResponse)
        assertEquals(completeTicket,messageResponse)
    }

    private fun createTicketRequest(request:TicketRequest, userID: Long = USER_ID) =
        mvc.perform(
            MockMvcRequestBuilders.post(BASE_URI)
                .header(HttpHeaderName.USER_ID, userID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(ObjectMapper().writeValueAsString(request))
                .characterEncoding(Charsets.UTF_8)
        )
    private fun updateTicketRequest(request: TicketRequest, id: Long) =
        mvc.perform(
            MockMvcRequestBuilders.put("$BASE_URI/$id")
                .header(HttpHeaderName.USER_ID, USER_ID)
                .content(ObjectMapper().writeValueAsString(request))
                .contentType(MediaType.APPLICATION_JSON)
        )

    private fun swipeTicketRequest(id: Long) =
        mvc.perform(
            MockMvcRequestBuilders.patch("$BASE_URI/swipe/$id")
                .header(HttpHeaderName.USER_ID, USER_ID)
        )

    private fun cancelSwipeTicketRequest(id: Long) =
        mvc.perform(
            MockMvcRequestBuilders.delete("$BASE_URI/swipe/$id")
                .header(HttpHeaderName.USER_ID, USER_ID)
        )
    private fun ResultActions.expectBindingException(): ResultActions{
        return andExpect(MockMvcResultMatchers.status().isBadRequest)
            .andExpect(MockMvcResultMatchers.jsonPath("field").isString)
            .andExpect(MockMvcResultMatchers.jsonPath("code").value(400))
            .andExpect(MockMvcResultMatchers.jsonPath("message").isString)
            .andExpect(MockMvcResultMatchers.jsonPath("detail").isString)
    }
}
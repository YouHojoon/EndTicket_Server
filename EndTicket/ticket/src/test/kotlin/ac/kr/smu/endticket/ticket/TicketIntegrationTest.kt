package ac.kr.smu.endticket.ticket

import ac.kr.smu.endticket.ticket.ui.request.TicketRequest
import ac.kr.smu.endticket.common.kafka.constant.KafkaTopic
import ac.kr.smu.endticket.common.kafka.test.createKafkaContainer
import ac.kr.smu.endticket.common.kafka.test.messageListener
import ac.kr.smu.endticket.common.web.aop.BindExceptionAdvice
import ac.kr.smu.endticket.common.web.enum.Color
import ac.kr.smu.endticket.common.web.enum.TicketType
import ac.kr.smu.endticket.common.web.test.andReturn
import ac.kr.smu.endticket.common.web.test.expectBindingException
import ac.kr.smu.endticket.common.web.test.expectExceptionResponse
import ac.kr.smu.endticket.ticket.domain.model.Ticket
import ac.kr.smu.endticket.ticket.domain.repository.TicketCompletedEventRepository
import ac.kr.smu.endticket.ticket.domain.repository.TicketRepository
import ac.kr.smu.endticket.ticket.infra.config.KafkaConfig
import ac.kr.smu.endticket.ticket.infra.messaging.TicketCompletedEventResponse
import ac.kr.smu.endticket.ticket.listener.TicketCompletedEventListener
import ac.kr.smu.endticket.ticket.service.TicketCompletedEventService
import ac.kr.smu.endticket.ticket.service.TicketService
import ac.kr.smu.endticket.ticket.ui.controller.TicketController
import ac.kr.smu.endticket.ticket.ui.response.TicketResponse
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
import org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration
import org.springframework.boot.autoconfigure.transaction.TransactionAutoConfiguration
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.kafka.listener.KafkaMessageListenerContainer
import org.springframework.kafka.test.EmbeddedKafkaBroker
import org.springframework.kafka.test.context.EmbeddedKafka
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.transaction.annotation.EnableTransactionManagement
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.TimeUnit
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@SpringBootTest(
    classes = [
        DataSourceAutoConfiguration::class,
        TransactionAutoConfiguration::class,
        HibernateJpaAutoConfiguration::class,
        KafkaAutoConfiguration::class,
        KafkaConfig::class,
        TicketCompletedEventService::class,
        TicketController::class,
        TicketService::class,
        TicketCompletedEventListener::class
    ]
)
@EnableTransactionManagement
@EmbeddedKafka
@EnableJpaRepositories("ac.kr.smu.endTicket.ticket.domain.repository")
@EntityScan("ac.kr.smu.endTicket.ticket.domain.model")
class TicketIntegrationTest @Autowired constructor(
    controller: TicketController,
    private val ticketRepository: TicketRepository,
    private val eventRepository: TicketCompletedEventRepository,
    private val broker: EmbeddedKafkaBroker
) {
    private val mvc: MockMvc =
        MockMvcBuilders.standaloneSetup(controller)
            .setControllerAdvice(BindExceptionAdvice())
            .build()

    private lateinit var container: KafkaMessageListenerContainer<String, TicketCompletedEventResponse>

    @AfterEach
    fun reset(){
        eventRepository.deleteAll()
        ticketRepository.deleteAll()
    }

    @Test
    @DisplayName("티켓 생성 테스트")
    fun given_ticketRequest_when_createTicket_then_expectStatusCode204_and_responseCreatedTicket(){
        mvc.createTicket(TICKET_REQUEST)
            .andExpect(MockMvcResultMatchers.status().isCreated)
            .andExpect(MockMvcResultMatchers.jsonPath("behavior").value(TICKET_REQUEST.behavior))
            .andExpect(MockMvcResultMatchers.jsonPath("target").value(TICKET_REQUEST.target))
            .andExpect(MockMvcResultMatchers.jsonPath("color").value(TICKET_REQUEST.color.name))
            .andExpect(MockMvcResultMatchers.jsonPath("type").value(TICKET_REQUEST.type.name))
            .andExpect(MockMvcResultMatchers.jsonPath("maxSwipeCount").value(TICKET_REQUEST.maxSwipeCount.name))
            .andExpect(MockMvcResultMatchers.jsonPath("swipeCount").value(0))
    }

    @ParameterizedTest
    @DisplayName("비정상적인 티켓 생성 테스트")
    @MethodSource("${TicketTestParameters.PATH}#provideInvalidRequest")
    fun given_invalidTicketRequest_when_createTicket_then_responseBindingExceptionResponseWithStatus400(request: TicketRequest){
        mvc.createTicket(request).expectBindingException()
    }

    @Test
    @DisplayName("티캣 개수 제한 이상으로 생성 테스트")
    fun given_userHasReachedTicketLimit_when_createTicket_then_responseExceptionResponseWithStatus409(){
        repeat(5){
            mvc.createTicket(TICKET_REQUEST)
        }

        mvc.createTicket(TICKET_REQUEST)
            .andExpect(MockMvcResultMatchers.status().isConflict)
            .expectExceptionResponse()
    }

    @Test
    @DisplayName("티켓 수정 테스트")
    fun given_ticketRequest_when_updateTicket_then_responseUpdatedTicket(){
        val ticket = mvc.createTicket(TICKET_REQUEST).andReturn<TicketResponse>()

        mvc.updateTicket(UPDATE_REQUEST, ticket.id)
            .andExpect(MockMvcResultMatchers.status().isOk)
    }

    @ParameterizedTest
    @DisplayName("비정상적인 티켓 수정 요청 테스트")
    @MethodSource("${TicketTestParameters.PATH}#provideInvalidRequest")
    fun given_invalidTicketRequest_when_updateTicket_then_responseBindingExceptionResponseWithStatus400(request: TicketRequest){
        val ticket = mvc.createTicket(TICKET_REQUEST).andReturn<TicketResponse>()
        mvc.updateTicket(request,ticket.id).expectBindingException()
    }

    @Test
    @DisplayName("존재하지 않는 티켓 수정 요청 테스트")
    fun given_notExistTicket_when_updateTicket_then_responseExceptionResponseWithStatus404() =
        mvc
            .updateTicket(TICKET_REQUEST, 1L)
            .andExpect(MockMvcResultMatchers.status().isNotFound)
            .expectExceptionResponse()


    @Test
    @DisplayName("티켓의 소유자가 아닌 사용자의 티켓 수정 요청 테스트")
    fun given_userWhoNotOwnerOfTicket_when_updateTicket_then_responseExceptionResponseWithStatus403(){
       val ticket = mvc.createTicket(TICKET_REQUEST, 2L).andReturn<TicketResponse>()

        mvc.updateTicket(TICKET_REQUEST, ticket.id)
            .andExpect(MockMvcResultMatchers.status().isForbidden)
            .expectExceptionResponse()
    }
    @Test
    @DisplayName("티켓 스와이프 테스트")
    fun given_Id_when_swipeTicket_then_responseSwipedTicket(){
        val ticket = mvc.createTicket(TICKET_REQUEST).andReturn<TicketResponse>()

        mvc.swipeTicket(ticket.id)
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.jsonPath("swipeCount").value(ticket.swipeCount + 1))
    }

    @Test
    @DisplayName("존재하지 않는 티켓 스와이프 테스트")
    fun given_notExistTicket_when_swipeTicket_then_responseExceptionResponseWithStatus404() =
        mvc
            .swipeTicket(1L)
            .andExpect(MockMvcResultMatchers.status().isNotFound)
            .expectExceptionResponse()


    @Test
    @DisplayName("티켓의 소유자가 아닌 사용자의 스와이프 테스트")
    fun given_userWhoNotOwnerOfTicket_when_swipeTicket_then_responseExceptionResponseWithStatus403(){
        val ticket = mvc.createTicket(TICKET_REQUEST, 2L).andReturn<TicketResponse>()

        mvc.swipeTicket(ticket.id)
            .andExpect(MockMvcResultMatchers.status().isForbidden)
            .expectExceptionResponse()
    }

    @Test
    @DisplayName("티켓 스와이프 취소 테스트")
    fun given_Id_when_cancelSwipeTicket_then_responseSwipeCanceledTicket(){
        val ticket = mvc.createTicket(TICKET_REQUEST).andReturn<TicketResponse>()
        val beforeSwipeCount = mvc.swipeTicket(ticket.id).andReturn<TicketResponse>().swipeCount

        mvc.cancelSwipeTicket(ticket.id)
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.jsonPath("swipeCount").value(beforeSwipeCount - 1))
    }

    @Test
    @DisplayName("존재하지 않는 티켓 스와이프 취소 테스트")
    fun given_notExistTicket_when_cancelSwipeTicket_then_responseExceptionResponseWithStatus404() =
        mvc
            .cancelSwipeTicket(1L)
            .andExpect(MockMvcResultMatchers.status().isNotFound)
            .expectExceptionResponse()


    @Test
    @DisplayName("소유자가 아닌 사용자 티켓 스와이프 취소 테스트")
    fun given_userWhoNotOwnerOfTicket_when_cancelSwipeTicket_then_responseExceptionResponseWithStatus403(){
        val ticket = mvc.createTicket(TICKET_REQUEST, 2L).andReturn<TicketResponse>()

        mvc.cancelSwipeTicket(ticket.id)
            .andExpect(MockMvcResultMatchers.status().isForbidden)
            .expectExceptionResponse()
    }

    @Test
    @DisplayName("미완료된 티켓 조회")
    fun given_userId_when_findIncompleteTickets_then_responseIncompleteTickets(){
       val ticket = mvc.createTicket(TICKET_REQUEST).andReturn<TicketResponse>()

        val json = mvc.findTickets()
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
        val ticket = mvc.createTicket(TICKET_REQUEST).andReturn<TicketResponse>()

        repeat(ticket.maxSwipeCount.value - 1){
            mvc.swipeTicket(ticket.id)
        }

        val completeTicket = mvc.swipeTicket(ticket.id)
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.jsonPath("swipeCount").value(ticket.maxSwipeCount.value))
            .andReturn<TicketResponse>()

        val queue = LinkedBlockingQueue<ConsumerRecord<String, TicketCompletedEventResponse>>()

        container = createKafkaContainer(broker, KafkaTopic.TICKET_COMPLETION)
        container.messageListener(broker){
            queue.add(it)
        }

        val record = queue.poll(1000, TimeUnit.MILLISECONDS)
        val response = record.value()

        assertNotNull(record)
        assertEquals(USER_ID, record.key().toLong())
        assertEquals(completeTicket.id, response.id)
        assertEquals(completeTicket.behavior, response.behavior)
        assertEquals(completeTicket.target, response.target)
        assertEquals(completeTicket.color, response.color)
        assertEquals(completeTicket.swipeCount, response.swipeCount)

    }

    @Test
    @DisplayName("티켓 삭제 테스트")
    fun given_id_when_deleteTicket_then_expectStatusCode204(){
        val ticket = mvc.createTicket(TICKET_REQUEST).andReturn<TicketResponse>()
        mvc.deleteTicket(ticket.id)
            .andExpect(MockMvcResultMatchers.status().isNoContent)
    }

    @Test
    @DisplayName("존재하지 않는 티켓 삭제 테스트")
    fun given_notExistTicket_when_deleteTicket_then_responseExceptionResponseWithStatus404(){
        mvc.deleteTicket(1L)
            .andExpect(MockMvcResultMatchers.status().isNotFound)
            .expectExceptionResponse()
    }

    @Test
    @DisplayName("소유자가 아닌 사용자의 티켓 삭제 테스트")
    fun given_userWhoNotOwner_when_deleteTicket_then_responseExceptionResponseWithStatus403(){
        val ticket = mvc.createTicket(TICKET_REQUEST, 2L).andReturn<TicketResponse>()
        mvc.deleteTicket(ticket.id)
            .andExpect(MockMvcResultMatchers.status().isForbidden)
            .expectExceptionResponse()
    }
}
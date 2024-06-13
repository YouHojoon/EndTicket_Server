package ac.kr.smu.endTicket.futureMe.imagination

import ac.kr.smu.endTicket.common.kafka.constant.KafkaTopic
import ac.kr.smu.endTicket.common.kafka.test.createKafkaContainer
import ac.kr.smu.endTicket.common.kafka.test.messageListener
import ac.kr.smu.endTicket.common.web.aop.BindExceptionAdvice
import ac.kr.smu.endTicket.common.web.test.andReturn
import ac.kr.smu.endTicket.common.web.test.expectBindingException
import ac.kr.smu.endTicket.common.web.test.expectExceptionResponse
import ac.kr.smu.endTicket.futureMe.domain.event.model.ImaginationCompletionEvent
import ac.kr.smu.endTicket.futureMe.domain.event.repository.EventRepository
import ac.kr.smu.endTicket.futureMe.domain.imagination.exception.NotFoundImaginationException
import ac.kr.smu.endTicket.futureMe.domain.imagination.exception.NotOwnerOfImaginationException
import ac.kr.smu.endTicket.futureMe.domain.imagination.model.Imagination
import ac.kr.smu.endTicket.futureMe.domain.imagination.repository.ImaginationRepository
import ac.kr.smu.endTicket.futureMe.infra.messaging.ImaginationCompletionEventMessageService
import ac.kr.smu.endTicket.futureMe.infra.messaging.ImaginationCompletionEventResponse
import ac.kr.smu.endTicket.futureMe.listener.ImaginationCompletionEventListener
import ac.kr.smu.endTicket.futureMe.service.FutureMeEventService
import ac.kr.smu.endTicket.futureMe.service.FutureMeService
import ac.kr.smu.endTicket.futureMe.service.ImaginationService
import ac.kr.smu.endTicket.futureMe.ui.controller.ImaginationController
import ac.kr.smu.endTicket.futureMe.ui.request.ImaginationRequest
import ac.kr.smu.endTicket.futureMe.ui.response.ImaginationResponse
import ac.kr.smu.endTicket.test.mockAny
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
import org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration
import org.springframework.boot.autoconfigure.transaction.TransactionAutoConfiguration
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.kafka.listener.KafkaMessageListenerContainer
import org.springframework.kafka.test.EmbeddedKafkaBroker
import org.springframework.kafka.test.context.EmbeddedKafka
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.TimeUnit
import kotlin.test.AfterTest
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@SpringBootTest(
    classes = [
        FutureMeService::class,
        ImaginationCompletionEventListener::class,
        ImaginationCompletionEventMessageService::class,
        ImaginationController::class,
        ImaginationService::class,
        FutureMeEventService::class,
        DataSourceAutoConfiguration::class,
        HibernateJpaAutoConfiguration::class,
        TransactionAutoConfiguration::class,
        KafkaAutoConfiguration::class,
    ]
)
@EmbeddedKafka(
    ports = [9292],
    brokerProperties = [
        "listeners=PLAINTEXT://localhost:9292"
    ],
    partitions = 3
)
@EnableJpaRepositories("ac.kr.smu.endTicket.futureMe.domain")
@EntityScan("ac.kr.smu.endTicket.futureMe.domain")
class ImaginationIntegrationTest @Autowired constructor(
    @MockBean
    private val eventRepo: EventRepository,
    @MockBean
    private val futureMeService: FutureMeService,

    private val repo: ImaginationRepository,
    private val broker: EmbeddedKafkaBroker,
    controller: ImaginationController
) {
    private val mvc: MockMvc = MockMvcBuilders.standaloneSetup(controller).setControllerAdvice(BindExceptionAdvice()).build()

    @AfterTest
    fun reset(){
        repo.deleteAll()
    }

    @Test
    @DisplayName("상상해보기 생성 테스트")
    fun given_request_when_createImagination_then_responseCreatedImagination() {
        mvc
            .createImagination(request)
            .andExpect(MockMvcResultMatchers.status().isCreated)
            .andExpect(MockMvcResultMatchers.jsonPath("behavior").value(request.behavior))
            .andExpect(MockMvcResultMatchers.jsonPath("target").value(request.target))
            .andExpect(MockMvcResultMatchers.jsonPath("color").value(request.color.name))
    }

    @Test
    @DisplayName("상상해보기 조회 테스트")
    fun given_user_when_findImaginations_then_responseImaginations(){
        mvc.createImagination(request)

        mvc.findImaginations()
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.jsonPath("imaginations").isNotEmpty)
    }


    @Test
    @DisplayName("비정상적인 상상해보기 생성 테스트")
    fun given_invalidRequest_when_createImagination_then_expectStatusCode400_and_responseBindExceptionResponse(){
        mvc.createImagination(invalidBehaviorRequest)
            .expectBindingException()
        mvc.createImagination(invalidTargetRequest)
            .expectBindingException()
    }

    @Test
    @DisplayName("최대 개수 이상으로 상상해보기 생성 테스트")
    fun given_requestExceedImaginationLimit_when_createImagination_then_expectStatusCode409_and_responseExceptionResponse(){
        repeat(6){
            mvc.createImagination(request)
        }

        mvc.createImagination(request)
            .andExpect(MockMvcResultMatchers.status().isConflict)
            .expectExceptionResponse()
    }

    @Test
    @DisplayName("상상해보기 수정 테스트")
    fun given_request_when_updateImagination_then_responseUpdatedImagination(){
        val request = ImaginationRequest(
            behavior = "new behav",
            target = "new target",
            color = Imagination.Color.GRAY2
        )
        val imagination = mvc.createImagination(request).andReturn<ImaginationResponse>()

        mvc.updateImagination(request, imagination.id, USER_ID)
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.jsonPath("behavior").value(request.behavior))
            .andExpect(MockMvcResultMatchers.jsonPath("target").value(request.target))
            .andExpect(MockMvcResultMatchers.jsonPath("color").value(request.color.name))

    }

    @Test
    @DisplayName("비정상적인 상상해보기 수정 테스트")
    fun given_invalidRequest_when_updateImagination_then_expectStatusCode400_and_responseBindExceptionResponse(){
        val imagination = mvc.createImagination(request).andReturn<ImaginationResponse>()

        mvc.updateImagination(invalidBehaviorRequest, imagination.id, USER_ID)
            .expectBindingException()
        mvc.updateImagination(invalidTargetRequest, imagination.id, USER_ID)
            .expectBindingException()
    }

    @Test
    @DisplayName("존재하지 않는 상상해보기 수정 테스트")
    fun given_notExistImagination_when_updateImagination_then_expectStatusCode404_and_responseExceptionResponse(){
        mvc.updateImagination(request,1L, USER_ID)
            .andExpect(MockMvcResultMatchers.status().isNotFound)
            .expectExceptionResponse()
    }

    @Test
    @DisplayName("소유자가 아닌 사용자 상상해보기 수정 테스트")
    fun given_userWhoNotOwner_when_updateImagination_then_expectStatusCode404_and_responseExceptionResponse(){
        val imagination = mvc.createImagination(request).andReturn<ImaginationResponse>()

        mvc.updateImagination(request,imagination.id, 2L)
            .andExpect(MockMvcResultMatchers.status().isForbidden)
            .expectExceptionResponse()
    }

    @Test
    @DisplayName("상상해보기 삭제 테스트")
    fun given_id_when_deleteImagination_then_expectStatusCode204(){
        val imagination = mvc.createImagination(request).andReturn<ImaginationResponse>()

        mvc.deleteImagination(imagination.id)
            .andExpect(MockMvcResultMatchers.status().isNoContent)
    }

    @Test
    @DisplayName("존재하지 않는 상상해보기 삭제 테스트")
    fun given_notExistImagination_when_deleteImagination_then_expectStatusCode404_and_responseExceptionResponse(){
        mvc.deleteImagination(1L)
            .andExpect(MockMvcResultMatchers.status().isNotFound)
            .expectExceptionResponse()
    }

    @Test
    @DisplayName("소유자가 아닌 사용자의 상상해보기 삭제 테스트")
    fun given_userWhoNotOwner_when_deleteImagination_then_expectStatusCode403_and_responseExceptionResponse(){
        val imagination = mvc.createImagination(request).andReturn<ImaginationResponse>()

        mvc.deleteImagination(imagination.id, 2L)
            .andExpect(MockMvcResultMatchers.status().isForbidden)
            .expectExceptionResponse()
    }
    @Test
    @DisplayName("상상해보기 완료 테스트")
    fun given_id_when_completeImagination_then_expectStatusCode204_and_sendImaginationCompletionEvent_and_gainExperiencePoints(){
        val container: KafkaMessageListenerContainer<String, ImaginationCompletionEventResponse> = createKafkaContainer(broker, KafkaTopic.IMAGINATION_COMPLETION)
        val queue = LinkedBlockingQueue<ConsumerRecord<String, ImaginationCompletionEventResponse>>()

        container.messageListener(broker){
            queue.add(it)
        }
        val imagination = mvc.createImagination(request).andReturn<ImaginationResponse>()

        mvc.completeImagination(imagination.id)
            .andExpect(MockMvcResultMatchers.status().isNoContent)

        Mockito.verify(eventRepo, Mockito.times(1)).save(mockAny())
        Mockito.verify(futureMeService, Mockito.times(1)).gainExperiencePoints(mockAny())

        val record = queue.poll(500, TimeUnit.MILLISECONDS)

        assertNotNull(record)
        assertEquals(USER_ID, record.key().toLong())
        assertEquals(record.value().id, imagination.id)
        assertEquals(record.value().behavior, imagination.behavior)
        assertEquals(record.value().color, imagination.color)
        assertEquals(record.value().target, imagination.target)
    }

    @Test
    @DisplayName("존재하지 않는 상상해보기 완료 테스트")
    fun given_notExistImagination_when_completeImagination_then_throwNotFoundImaginationException(){
        mvc.completeImagination(1L)
            .andExpect(MockMvcResultMatchers.status().isNotFound)
            .expectExceptionResponse()
    }

    @Test
    @DisplayName("소유자가 아닌 사용자의 상상해보기 완료 테스트")
    fun given_userWhoNotOwner_when_completeImagination_then_throwNotOwnerOfImagination(){
        val imagination = mvc.createImagination(request).andReturn<ImaginationResponse>()

        mvc.completeImagination(imagination.id, 2L)
            .andExpect(MockMvcResultMatchers.status().isForbidden)
            .expectExceptionResponse()
    }
}

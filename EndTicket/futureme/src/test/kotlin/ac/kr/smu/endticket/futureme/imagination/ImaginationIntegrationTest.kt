package ac.kr.smu.endticket.futureme.imagination


import ac.kr.smu.endticket.common.kafka.constant.KafkaTopic
import ac.kr.smu.endticket.common.kafka.test.createKafkaContainer
import ac.kr.smu.endticket.common.kafka.test.messageListener
import ac.kr.smu.endticket.common.web.aop.BindExceptionAdvice
import ac.kr.smu.endticket.common.web.enum.CharacterType
import ac.kr.smu.endticket.common.web.test.andReturn
import ac.kr.smu.endticket.common.web.test.expectBindException
import ac.kr.smu.endticket.common.web.test.expectExceptionResponse
import ac.kr.smu.endticket.futureme.domain.event.model.ImaginationCompletedEvent
import ac.kr.smu.endticket.futureme.domain.event.repository.EventRepository
import ac.kr.smu.endticket.futureme.domain.imagination.repository.ImaginationRepository
import ac.kr.smu.endticket.futureme.config.KafkaConfig
import ac.kr.smu.endticket.futureme.infra.messaging.ImaginationCompletedEventResponse
import ac.kr.smu.endticket.futureme.listener.ImaginationCompletedEventListener
import ac.kr.smu.endticket.futureme.service.FutureMeEventService
import ac.kr.smu.endticket.futureme.service.FutureMeService
import ac.kr.smu.endticket.futureme.service.ImaginationService
import ac.kr.smu.endticket.futureme.ui.controller.ImaginationController
import ac.kr.smu.endticket.futureme.ui.request.CreateFutureMeRequest
import ac.kr.smu.endticket.futureme.ui.request.ImaginationRequest
import ac.kr.smu.endticket.futureme.ui.response.ImaginationResponse
import org.apache.kafka.clients.consumer.ConsumerRecord
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
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.TimeUnit
import kotlin.test.*

@SpringBootTest(
    classes = [
        FutureMeService::class,
        ImaginationCompletedEventListener::class,
        ImaginationController::class,
        ImaginationService::class,
        FutureMeEventService::class,
        DataSourceAutoConfiguration::class,
        HibernateJpaAutoConfiguration::class,
        TransactionAutoConfiguration::class,
        KafkaAutoConfiguration::class,
        KafkaConfig::class
    ]
)
@EmbeddedKafka(partitions = 3)
@EnableJpaRepositories("ac.kr.smu.endticket.futureme.domain")
@EntityScan("ac.kr.smu.endticket.futureme.domain")
class ImaginationIntegrationTest @Autowired constructor(
    private val eventRepo: EventRepository,
    private val futureMeService: FutureMeService,
    private val repo: ImaginationRepository,
    private val broker: EmbeddedKafkaBroker,
    controller: ImaginationController
) {
    private val mvc: MockMvc = MockMvcBuilders.standaloneSetup(controller).setControllerAdvice(BindExceptionAdvice()).build()
    @BeforeTest
    fun init(){
        futureMeService.createFutureMe(CreateFutureMeRequest(CharacterType.CHEESE),ImaginationParameters.USER_ID)
    }
    @AfterTest
    fun reset(){
        futureMeService.deleteFutureMe(ImaginationParameters.USER_ID)
        eventRepo.deleteAll()
        repo.deleteAll()
    }

    @Test
    @DisplayName("상상해보기 생성 테스트")
    fun given_request_when_createImagination_then_responseCreatedImagination() {
        mvc
            .createImagination(ImaginationParameters.REQUEST)
            .andExpect(MockMvcResultMatchers.status().isCreated)
            .andExpect(MockMvcResultMatchers.jsonPath("behavior").value(ImaginationParameters.REQUEST.behavior))
            .andExpect(MockMvcResultMatchers.jsonPath("target").value(ImaginationParameters.REQUEST.target))
            .andExpect(MockMvcResultMatchers.jsonPath("color").value(ImaginationParameters.REQUEST.color.name))
    }

    @Test
    @DisplayName("상상해보기 조회 테스트")
    fun given_user_when_findImaginations_then_responseImaginations(){
        mvc.createImagination(ImaginationParameters.REQUEST)

        mvc.findImaginations()
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.jsonPath("imaginations").isNotEmpty)
    }


    @ParameterizedTest
    @DisplayName("비정상적인 상상해보기 생성 요청 테스트")
    @MethodSource("${ImaginationParameters.PATH}#provideInvalidImaginationRequest")
    fun given_invalidRequest_when_createImagination_then_responseBindExceptionResponseWithStatus400(request: ImaginationRequest){
        mvc.createImagination(request)
    }

    @Test
    @DisplayName("최대 개수 이상으로 상상해보기 생성 테스트")
    fun given_requestExceedImaginationLimit_when_createImagination_then_responseExceptionResponseWithStatus409(){
        repeat(6){
            mvc.createImagination(ImaginationParameters.REQUEST)
        }

        mvc.createImagination(ImaginationParameters.REQUEST)
            .andExpect(MockMvcResultMatchers.status().isConflict)
            .expectExceptionResponse()
    }

    @Test
    @DisplayName("상상해보기 수정 테스트")
    fun given_request_when_updateImagination_then_responseUpdatedImagination(){
        val imagination = mvc.createImagination(ImaginationParameters.REQUEST).andReturn<ImaginationResponse>()

        mvc.updateImagination(ImaginationParameters.UPDATE_REQUEST, imagination.id, ImaginationParameters.USER_ID)
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.jsonPath("behavior").value(ImaginationParameters.UPDATE_REQUEST.behavior))
            .andExpect(MockMvcResultMatchers.jsonPath("target").value(ImaginationParameters.UPDATE_REQUEST.target))
            .andExpect(MockMvcResultMatchers.jsonPath("color").value(ImaginationParameters.UPDATE_REQUEST.color.name))

    }

    @ParameterizedTest
    @DisplayName("비정상적인 상상해보기 수정 요청 테스트")
    @MethodSource("${ImaginationParameters.PATH}#provideInvalidImaginationRequest")
    fun given_invalidRequest_when_updateImagination_then_responseBindExceptionResponseWithStatus400(request: ImaginationRequest){
        val imagination = mvc.createImagination(ImaginationParameters.REQUEST).andReturn<ImaginationResponse>()

        mvc.updateImagination(request, imagination.id, ImaginationParameters.USER_ID)
            .expectBindException()
    }

    @Test
    @DisplayName("존재하지 않는 상상해보기 수정 테스트")
    fun given_notExistsImaginationId_when_updateImagination_then_responseExceptionResponseWithStatus404(){
        mvc.updateImagination(ImaginationParameters.REQUEST, 1L, ImaginationParameters.USER_ID)
            .andExpect(MockMvcResultMatchers.status().isNotFound)
            .expectExceptionResponse()
    }


    @Test
    @DisplayName("소유자가 아닌 사용자 상상해보기 수정 테스트")
    fun given_userIdWhoNotOwner_when_updateImagination_then_responseExceptionResponseWithStatus403(){
        val imagination = mvc.createImagination(ImaginationParameters.REQUEST, ImaginationParameters.USER_ID).andReturn<ImaginationResponse>()

        mvc.updateImagination(ImaginationParameters.REQUEST, imagination.id, 2L)
            .andExpect(MockMvcResultMatchers.status().isForbidden)
            .expectExceptionResponse()
    }

    @Test
    @DisplayName("상상해보기 삭제 테스트")
    fun given_id_when_deleteImagination_then_expectStatus204(){
        val imagination = mvc.createImagination(ImaginationParameters.REQUEST).andReturn<ImaginationResponse>()

        mvc.deleteImagination(imagination.id)
            .andExpect(MockMvcResultMatchers.status().isNoContent)
    }

    @Test
    @DisplayName("존재하지 않는 상상해보기 삭제 테스트")
    fun given_notExistImagination_when_deleteImagination_then_responseExceptionResponseWithStatus404(){
        mvc.deleteImagination(1L)
            .andExpect(MockMvcResultMatchers.status().isNotFound)
            .expectExceptionResponse()
    }

    @Test
    @DisplayName("소유자가 아닌 사용자의 상상해보기 삭제 테스트")
    fun given_userWhoNotOwner_when_deleteImagination_then_responseExceptionResponseWithStatus403(){
        val imagination = mvc.createImagination(ImaginationParameters.REQUEST).andReturn<ImaginationResponse>()

        mvc.deleteImagination(imagination.id, 2L)
            .andExpect(MockMvcResultMatchers.status().isForbidden)
            .expectExceptionResponse()
    }
    @Test
    @DisplayName("상상해보기 완료 테스트")
    fun given_id_when_completeImagination_then_expectStatusCode204_and_sendImaginationCompletionEventAndGainExperiencePoints(){
        val container: KafkaMessageListenerContainer<String, ImaginationCompletedEventResponse> = createKafkaContainer(broker, KafkaTopic.IMAGINATION_COMPLETED)
        val queue = LinkedBlockingQueue<ConsumerRecord<String, ImaginationCompletedEventResponse>>()

        container.messageListener(broker){
            queue.add(it)
        }
        val imagination = mvc.createImagination(ImaginationParameters.REQUEST).andReturn<ImaginationResponse>()

        mvc.completeImagination(imagination.id)
            .andExpect(MockMvcResultMatchers.status().isNoContent)

        val record = queue.poll(500, TimeUnit.MILLISECONDS)
        val futureMe = futureMeService.findFutureMe(ImaginationParameters.USER_ID)

        //레코드 검증
        assertNotNull(record)
        assertEquals(ImaginationParameters.USER_ID, record.key().toLong())
        assertEquals(imagination.id, record.value().id)
        assertEquals(imagination.behavior, record.value().behavior)
        assertEquals(imagination.color, record.value().color)
        assertEquals(imagination.target, record.value().target)
        assertEquals(futureMe.character.type, record.value().characterType)

        //경험치 상승 검증
        assertEquals(10,futureMe.character.experiencePoints)
    }

    @Test
    @DisplayName("존재하지 않는 상상해보기 완료 테스트")
    fun given_notExistImagination_when_completeImagination_then_responseExceptionResponseWithStatus404(){
        mvc.completeImagination(1L)
            .andExpect(MockMvcResultMatchers.status().isNotFound)
            .expectExceptionResponse()
    }

    @Test
    @DisplayName("소유자가 아닌 사용자의 상상해보기 완료 테스트")
    fun given_userWhoNotOwner_when_completeImagination_then_responseExceptionResponseWithStatus403(){
        val imagination = mvc.createImagination(ImaginationParameters.REQUEST).andReturn<ImaginationResponse>()

        mvc.completeImagination(imagination.id, 2L)
            .andExpect(MockMvcResultMatchers.status().isForbidden)
            .expectExceptionResponse()
    }
}

package ac.kr.smu.endticket.user

import KafkaMessageService
import ac.kr.smu.endticket.common.kafka.constant.KafkaTopic
import ac.kr.smu.endticket.common.kafka.messaging.KafkaMessage
import ac.kr.smu.endticket.common.kafka.test.createKafkaContainer
import ac.kr.smu.endticket.common.kafka.test.messageListener
import ac.kr.smu.endticket.common.test.mockAny
import ac.kr.smu.endticket.user.domain.model.UserDeletedEvent
import ac.kr.smu.endticket.user.domain.repository.UserDeletedEventRepository
import ac.kr.smu.endticket.user.listener.UserDeletedEventListener
import ac.kr.smu.endticket.user.service.UserEventService
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.mockito.Mock
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.boot.test.mock.mockito.SpyBean
import org.springframework.kafka.listener.KafkaMessageListenerContainer
import org.springframework.kafka.test.EmbeddedKafkaBroker
import org.springframework.kafka.test.context.EmbeddedKafka
import java.util.concurrent.CompletableFuture
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.TimeUnit
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.assertNotNull

@SpringBootTest(
    classes = [
        UserDeletedEventListener::class,
        KafkaAutoConfiguration::class,
        UserEventService::class
    ]
)
@EmbeddedKafka
class UserDeletedEventListenerTest @Autowired constructor(
    @SpyBean
    private val messageService: KafkaMessageService<String, Void>,
    @MockBean
    private val repo: UserDeletedEventRepository,
    private val eventService: UserEventService,
    private val broker: EmbeddedKafkaBroker
) {
    private lateinit var container: KafkaMessageListenerContainer<String,Void>

    @BeforeTest
    fun init(){
        container = createKafkaContainer(broker, KafkaTopic.USER_DELETED)
    }

    @AfterTest
    fun reset(){
        container.stop()
    }

    @ParameterizedTest
    @DisplayName("사용자 탈퇴 이벤트 테스트")
    @MethodSource("${UserTestParameters.PATH}#provideEvent")
    fun given_userDeletedEvent_then_saveEventAndSendMessage(event: UserDeletedEvent){
        val queue = LinkedBlockingQueue<ConsumerRecord<String, Void>>()

        container.messageListener(broker){
            queue.add(it)
        }

        eventService.publish(event)

        val record = queue.poll(500L, TimeUnit.MILLISECONDS)

        Mockito.verify(repo).save(mockAny())
        Mockito.verify(repo).delete(mockAny())
        assertNotNull(record)
    }

    @ParameterizedTest
    @DisplayName("회원 탈퇴 이벤트 전송 실패 테스트")
    @MethodSource("${UserTestParameters.PATH}#provideEvent")
    fun given_userDeletedEvent_when_sendMessageFail_then_doNothing(event: UserDeletedEvent){
        val message = event.toMessage()
        Mockito.`when`(messageService.send(KafkaTopic.USER_DELETED, message))
            .thenReturn(CompletableFuture.failedFuture(RuntimeException()))

        eventService.publish(event)

        Mockito.verify(repo, Mockito.only()).save(event)
    }
}
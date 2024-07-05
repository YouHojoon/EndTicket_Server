package ac.kr.smu.endticket.user

import KafkaMessageService
import ac.kr.smu.endticket.common.kafka.constant.KafkaTopic
import ac.kr.smu.endticket.common.kafka.test.createKafkaContainer
import ac.kr.smu.endticket.common.kafka.test.messageListener
import ac.kr.smu.endticket.common.test.mockAny
import ac.kr.smu.endticket.user.domain.model.UserDeletedEvent
import ac.kr.smu.endticket.user.domain.repository.UserDeletedEventRepository
import ac.kr.smu.endticket.user.job.UserDeletedEventJob
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.boot.test.mock.mockito.SpyBean
import org.springframework.kafka.listener.KafkaMessageListenerContainer
import org.springframework.kafka.test.EmbeddedKafkaBroker
import org.springframework.kafka.test.context.EmbeddedKafka
import java.util.concurrent.BlockingQueue
import java.util.concurrent.CompletableFuture
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.TimeUnit
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.assertEquals

@SpringBootTest(
    classes = [
        UserDeletedEventJob::class,
        KafkaAutoConfiguration::class
    ]
)
@EmbeddedKafka
class UserDeletedEventJobTest @Autowired constructor(
    @SpyBean
    private val messageService: KafkaMessageService<String,Void>,
    @MockBean
    private val repo: UserDeletedEventRepository,
    private val job: UserDeletedEventJob,
    private val broker: EmbeddedKafkaBroker
) {
    private lateinit var container: KafkaMessageListenerContainer<String,Void>

    @BeforeTest
    fun init(){
        container = createKafkaContainer(broker,KafkaTopic.USER_DELETED)
    }

    @AfterTest
    fun reset(){
        container.stop()
    }

    @ParameterizedTest
    @MethodSource("${UserTestParameters.PATH}#provideEvents")
    @DisplayName("미전송된 회원 탈퇴 이벤트들 재전송 테스트")
    fun given_notSentUserDeletedEvents_when_resend_then_resendMessageAndDeleteEvent(events: Set<UserDeletedEvent>){
        val queue = LinkedBlockingQueue<ConsumerRecord<String,Void>>()
        container.messageListener(broker){
            queue.add(it)
        }

        Mockito.`when`(repo.findByAuditCreatedAtBefore(mockAny()))
            .thenReturn(events)

        job.resend()

        Thread.sleep(500L)
        assert(queue.isNotEmpty())
        for((message,event) in queue.zip(events))
            assertEquals(event.toMessage().key, message.key())
    }


    @ParameterizedTest
    @MethodSource("${UserTestParameters.PATH}#provideEvents")
    @DisplayName("미전송된 회원 탈퇴 이벤트들 재전송 실패 테스트")
    fun given_notSentUserDeletedEvent_when_resendFail_then_doNothing(events: Set<UserDeletedEvent>) {
        val messages = events.map { it.toMessage() }

        Mockito.`when`(repo.findByAuditCreatedAtBefore(mockAny())).thenReturn(
            events
        )
        Mockito.`when`(messageService.send(KafkaTopic.USER_DELETED, messages))
            .thenReturn(emptySet())

        job.resend()

        Mockito.verify(repo).deleteAllById(Mockito.argThat<List<Long>>{ it.isEmpty() })
    }
}
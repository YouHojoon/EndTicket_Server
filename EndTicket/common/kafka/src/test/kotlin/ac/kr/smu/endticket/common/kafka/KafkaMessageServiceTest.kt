package ac.kr.smu.endticket.common.kafka

import KafkaMessageService
import ac.kr.smu.endticket.common.kafka.messaging.KafkaMessage
import ac.kr.smu.endticket.common.kafka.test.createKafkaContainer
import ac.kr.smu.endticket.common.kafka.test.messageListener
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.kafka.test.EmbeddedKafkaBroker
import org.springframework.kafka.test.context.EmbeddedKafka
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.TimeUnit
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@SpringBootTest(
    classes = [KafkaMessageService::class, KafkaAutoConfiguration::class]
)
@EmbeddedKafka
class KafkaMessageServiceTest @Autowired constructor(
    private val service: KafkaMessageService<String, Map<String, String>>,
    private val broker: EmbeddedKafkaBroker
) {
    @Test
    @DisplayName("메시지 전송 테스트")
    fun given_topic_and_message_when_send_then_returnComputableFuture(){
        val topic = "test"
        val container = createKafkaContainer<Map<String,String>>(broker,topic)
        val queue = LinkedBlockingQueue<ConsumerRecord<String,Map<String,String>>>()
        val message = KafkaMessage("key", mapOf("payload" to "payload"))

        container.messageListener(broker){
            queue.add(it)
        }

        service.send(topic, message)

        val record = queue.poll(500,TimeUnit.MILLISECONDS)
        assertNotNull(record)
        assertEquals(message.key, record.key())
        assertEquals(message.payload, record.value())
    }

    @Test
    @DisplayName("메시지들 전송 테스트")
    fun given_topic_and_messages_when_send_then_returnComputableFuture(){
        val topic = "test"
        val container = createKafkaContainer<Map<String,String>>(broker,topic)
        val queue = LinkedBlockingQueue<ConsumerRecord<String,Map<String,String>>>()
        val messages = listOf(KafkaMessage("key", mapOf("payload" to "payload")))

        container.messageListener(broker){
            queue.add(it)
        }

        service.send(topic, messages)

        for ((message, record) in messages.zip(queue)){
            assertEquals(message.key, record.key())
            assertEquals(message.payload, record.value())
        }
    }
}
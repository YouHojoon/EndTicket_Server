package ac.kr.smu.endticket.common.kafka.handler

import org.apache.kafka.clients.consumer.ConsumerRecord
import org.slf4j.LoggerFactory
import org.springframework.kafka.support.KafkaHeaders
import org.springframework.messaging.handler.annotation.Header

class DLTHandler {
    private val log = LoggerFactory.getLogger(DLTHandler::class.java)

    fun handle(
        record: ConsumerRecord<String, Any>,
        @Header(KafkaHeaders.TOPIC) topic: String,
        @Header(KafkaHeaders.OFFSET) offset: Long,
        @Header(KafkaHeaders.GROUP_ID) groupId: String,
        @Header(KafkaHeaders.EXCEPTION_MESSAGE) exceptionMessage: String,
    ) {
        log.error(
            "DLT 발생 : {key: ${record.key()}, value: ${record.value()}, topic: $topic, offset: $offset, groupId: $groupId, exceptionMessage: $exceptionMessage}",
        )
    }
}

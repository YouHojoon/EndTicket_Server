package ac.kr.smu.endticket.futureme.service

import ac.kr.smu.endticket.common.kafka.constant.KafkaTopic
import ac.kr.smu.endticket.futureme.domain.event.model.TicketCompletedEvent
import ac.kr.smu.endticket.futureme.domain.event.repository.EventRepository
import ac.kr.smu.endticket.futureme.domain.futureme.exception.FutureMeNotFoundException
import ac.kr.smu.endticket.futureme.infra.messaging.TicketCompletedEventResponse
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.support.Acknowledgment
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Duration

/**
 * 티켓 완료를 처리하는 클래스
 * @property repo 이벤트를 저장하기 위한 저장소
 * @property futureMeService 경험치 증가를 위한 미래의 나 서비스
 */
@Service
class TicketCompletedEventConsumeService(
    private val repo: EventRepository,
    private val futureMeService: FutureMeService
) {
    private val log = LoggerFactory.getLogger(TicketCompletedEvent::class.java)

    /**
     * 티켓 완료 이벤트를 받는 메소드,
     * 이벤트를 저장하고 경험치를 올리는데 성공했다면 ack, 실패한다면 nack을 kafka에 기록한다.
     * @param record 이벤트의 내용
     * @param ack 이벤트의 처리결과를 kafka에 알리기 위한 객체
     */
    @KafkaListener(topics = [KafkaTopic.TICKET_COMPLETED])
    @Transactional
    fun consume(record: ConsumerRecord<String, TicketCompletedEventResponse>, ack: Acknowledgment){
        try {
            if (!repo.existsBySpecificIdAndType(record.value().id, TicketCompletedEvent::class)){
                val event = TicketCompletedEvent(record.value().id, record.key().toLong())

                futureMeService.gainExperiencePoints(event)
                repo.save(event)
            }

            ack.acknowledge()
        } catch (e: FutureMeNotFoundException) {
            // 이벤트를 수신 전에 회원 탈퇴가 되었다면 다음 오프셋으로 넘긴다.
            ack.acknowledge()
        } catch (e: Exception){
            log.error("티켓 완료 이벤트 수신 실패", e)
            
            ack.nack(
                Duration.ofSeconds(5)
            )
        }
    }
}
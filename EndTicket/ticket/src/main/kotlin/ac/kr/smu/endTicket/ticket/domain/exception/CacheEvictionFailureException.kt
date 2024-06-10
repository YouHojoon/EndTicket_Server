package ac.kr.smu.endTicket.ticket.domain.exception

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus

/**
 * 캐시 삭제 실패시 발생하는 에러
 */

@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
class CacheEvictionFailureException: RuntimeException()
package ac.kr.smu.endTicket.futureMe.domain.imagination.exception

/**
 * 상상해보기가 존재하지 않을 때 발생하는 에러
 * @property id 존재하지 않는 id
 */
class NotFoundImaginationException(
    private val id: Long
): RuntimeException("$id 의 상상해보기가 존재하지 않습니다.")
